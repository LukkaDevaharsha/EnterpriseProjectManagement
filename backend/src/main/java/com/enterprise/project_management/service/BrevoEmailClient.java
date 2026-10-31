package com.enterprise.project_management.service;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/** Sends transactional email through Brevo's HTTPS API without exposing provider details. */
@Service
public class BrevoEmailClient {
  private static final String EMAIL_ENDPOINT = "https://api.brevo.com/v3/smtp/email";
  private final RestClient restClient;
  private final String apiKey;
  private final String senderEmail;
  private final String senderName;

  public BrevoEmailClient(
      @org.springframework.beans.factory.annotation.Qualifier("brevoRestClient") RestClient restClient,
      @Value("${app.brevo.api-key:}") String apiKey,
      @Value("${app.brevo.sender-email:}") String senderEmail,
      @Value("${app.brevo.sender-name:}") String senderName) {
    this.restClient = restClient;
    this.apiKey = apiKey == null ? "" : apiKey.trim();
    this.senderEmail = senderEmail == null ? "" : senderEmail.trim();
    this.senderName = senderName == null ? "" : senderName.trim();
  }

  public void send(String recipient, String subject, String text, String replyTo) {
    if (apiKey.isBlank()) throw new IllegalStateException("Transactional email is not configured: BREVO_API_KEY is missing.");
    if (!isValidEmail(senderEmail) || senderName.isBlank()) throw new IllegalStateException("Transactional email sender is not configured correctly.");
    if (!isValidEmail(recipient)) throw new IllegalStateException("Transactional email recipient is invalid.");

    Map<String, Object> payload = new java.util.LinkedHashMap<>();
    payload.put("sender", Map.of("name", senderName, "email", senderEmail));
    payload.put("to", List.of(Map.of("email", recipient)));
    payload.put("subject", subject);
    payload.put("textContent", text);
    if (replyTo != null && !replyTo.isBlank()) payload.put("replyTo", Map.of("email", replyTo));

    try {
      restClient.post().uri(EMAIL_ENDPOINT)
          .header("api-key", apiKey)
          .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
          .body(payload).retrieve().toBodilessEntity();
    } catch (RestClientResponseException exception) {
      throw providerFailure(exception.getStatusCode().value());
    } catch (ResourceAccessException exception) {
      throw new IllegalStateException("Transactional email provider timed out or could not be reached.");
    }
  }

  private IllegalStateException providerFailure(int status) {
    if (status == 400) return new IllegalStateException("Brevo rejected the sender or email content (HTTP 400).");
    if (status == 401 || status == 403) return new IllegalStateException("Brevo rejected the configured API credentials (HTTP " + status + ").");
    if (status == 429) return new IllegalStateException("Brevo is rate limiting email delivery. Please retry later.");
    if (status >= 500) return new IllegalStateException("Brevo is temporarily unavailable. Please retry later.");
    return new IllegalStateException("Brevo could not accept the email request (HTTP " + status + ").");
  }

  private boolean isValidEmail(String email) {
    return email != null && email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
  }
}
