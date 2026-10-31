package com.enterprise.project_management.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import com.enterprise.project_management.entity.User;
import java.net.SocketTimeoutException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class EmailVerificationMailerTest {
  private static final String ENDPOINT = "https://api.brevo.com/v3/smtp/email";

  @Test
  void sendsSixDigitCodeThroughBrevoToTheRegisteredAddress() {
    RestClient.Builder builder = RestClient.builder();
    MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    server.expect(requestTo(ENDPOINT)).andExpect(method(HttpMethod.POST))
        .andExpect(header("api-key", "test-api-key"))
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().json("""
            {"sender":{"name":"Test Sender","email":"sender@example.com"},
             "to":[{"email":"recipient@example.com"}],
             "subject":"Verify your Enterprise Project Management account",
             "textContent":"Hello Test User,\\n\\nYour email verification code is 123456. It expires in 15 minutes.\\n\\nIf you did not create this account, you can ignore this email."}
            """))
        .andRespond(withStatus(HttpStatus.CREATED));

    BrevoEmailClient client = new BrevoEmailClient(builder.build(), "test-api-key", "sender@example.com", "Test Sender");
    EmailVerificationMailer mailer = new EmailVerificationMailer(client);
    User user = new User();
    user.setName("Test User");
    user.setEmail("recipient@example.com");

    mailer.sendCode(user, "123456");
    server.verify();
  }

  @Test
  void missingApiKeyFailsBeforeMakingARequest() {
    BrevoEmailClient client = new BrevoEmailClient(RestClient.builder().build(), "", "sender@example.com", "Sender");
    assertTrue(assertThrows(IllegalStateException.class,
        () -> client.send("recipient@example.com", "subject", "body", null)).getMessage().contains("BREVO_API_KEY"));
  }

  @Test
  void invalidSenderFailsBeforeMakingARequest() {
    BrevoEmailClient client = new BrevoEmailClient(RestClient.builder().build(), "key", "not-an-email", "Sender");
    assertThrows(IllegalStateException.class, () -> client.send("recipient@example.com", "subject", "body", null));
  }

  @Test
  void providerErrorsAreMeaningfulAndDoNotExposeResponseBody() {
    for (HttpStatus status : new HttpStatus[] {HttpStatus.BAD_REQUEST, HttpStatus.UNAUTHORIZED, HttpStatus.TOO_MANY_REQUESTS, HttpStatus.SERVICE_UNAVAILABLE}) {
      RestClient.Builder builder = RestClient.builder();
      MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
      server.expect(requestTo(ENDPOINT)).andRespond(withStatus(status).body("sensitive provider response"));
      BrevoEmailClient client = new BrevoEmailClient(builder.build(), "key", "sender@example.com", "Sender");

      IllegalStateException exception = assertThrows(IllegalStateException.class,
          () -> client.send("recipient@example.com", "subject", "body", null));
      assertFalse(exception.getMessage().contains("sensitive provider response"));
      server.verify();
    }
  }

  @Test
  void providerTimeoutReturnsSanitizedFailure() {
    RestClient.Builder builder = RestClient.builder();
    MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    server.expect(requestTo(ENDPOINT)).andRespond(request -> { throw new SocketTimeoutException("request timed out"); });
    BrevoEmailClient client = new BrevoEmailClient(builder.build(), "key", "sender@example.com", "Sender");

    assertThrows(IllegalStateException.class,
        () -> client.send("recipient@example.com", "subject", "sensitive OTP 123456", null));
  }
}
