package com.enterprise.project_management.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.enterprise.project_management.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

class SecurityConfigCorsTest {
  private static final String PRODUCTION = "https://enterprise-project-management-pi.vercel.app";
  private static final String PREVIEW = "https://enterprise-project-management-ae2q0wwq5-devaharsha119-3699.vercel.app";

  @Test
  void allowsProductionLocalhostAndRestrictedVercelPreviewButRejectsOtherOrigins() {
    SecurityConfig securityConfig = new SecurityConfig((JwtAuthenticationFilter) null);
    ReflectionTestUtils.setField(securityConfig, "frontendOrigin", PRODUCTION);
    CorsConfigurationSource source = securityConfig.corsConfigurationSource();
    CorsConfiguration configuration = source.getCorsConfiguration(
        new MockHttpServletRequest("POST", "/api/auth/register"));

    assertEquals(PRODUCTION, configuration.checkOrigin(PRODUCTION));
    assertEquals(PREVIEW, configuration.checkOrigin(PREVIEW));
    assertEquals("http://localhost:5173", configuration.checkOrigin("http://localhost:5173"));
    assertNull(configuration.checkOrigin("https://unrelated-project.vercel.app"));
    assertNull(configuration.checkOrigin("https://enterprise-project-management.evil.example"));
    assertTrue(configuration.getAllowedMethods().contains("OPTIONS"));
  }
}
