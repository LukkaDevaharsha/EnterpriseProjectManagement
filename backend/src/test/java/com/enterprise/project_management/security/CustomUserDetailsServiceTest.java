package com.enterprise.project_management.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.enterprise.project_management.entity.Role;
import com.enterprise.project_management.entity.User;
import com.enterprise.project_management.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {
  @Mock UserRepository userRepository;
  @InjectMocks CustomUserDetailsService userDetailsService;

  @Test
  void deactivatedAccountIsDisabledWhenJwtFilterReloadsUser() {
    User user = new User();
    user.setEmail("inactive@example.com");
    user.setPassword("encoded");
    user.setRole(Role.EMPLOYEE);
    user.setActive(false);
    user.setEmailVerified(true);
    when(userRepository.findByEmail("inactive@example.com")).thenReturn(Optional.of(user));

    UserDetails details = userDetailsService.loadUserByUsername("inactive@example.com");

    assertFalse(details.isEnabled());
  }

  @Test
  void unverifiedAccountIsDisabledAsWell() {
    User user = new User();
    user.setEmail("unverified@example.com");
    user.setPassword("encoded");
    user.setRole(Role.EMPLOYEE);
    user.setActive(true);
    user.setEmailVerified(false);
    when(userRepository.findByEmail("unverified@example.com")).thenReturn(Optional.of(user));

    assertFalse(userDetailsService.loadUserByUsername("unverified@example.com").isEnabled());
  }
}
