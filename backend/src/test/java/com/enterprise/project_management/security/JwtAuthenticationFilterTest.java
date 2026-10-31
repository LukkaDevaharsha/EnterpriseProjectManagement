package com.enterprise.project_management.security;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

class JwtAuthenticationFilterTest {
  @AfterEach
  void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void inactiveAccountWithPreviouslyValidJwtDoesNotReceiveAuthentication() throws Exception {
    JwtService jwtService = org.mockito.Mockito.mock(JwtService.class);
    CustomUserDetailsService userDetailsService = org.mockito.Mockito.mock(CustomUserDetailsService.class);
    when(jwtService.extractEmail("old-jwt")).thenReturn("inactive@example.com");
    when(userDetailsService.loadUserByUsername("inactive@example.com"))
        .thenReturn(User.withUsername("inactive@example.com").password("encoded").roles("EMPLOYEE").disabled(true).build());
    JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService, userDetailsService);

    MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/profile");
    request.addHeader("Authorization", "Bearer old-jwt");
    MockHttpServletResponse response = new MockHttpServletResponse();
    AtomicBoolean requestReachedAuthorizationChain = new AtomicBoolean();

    filter.doFilter(request, response, (req, res) -> {
      requestReachedAuthorizationChain.set(true);
      assertNull(SecurityContextHolder.getContext().getAuthentication());
    });

    assertTrue(requestReachedAuthorizationChain.get());
  }
}
