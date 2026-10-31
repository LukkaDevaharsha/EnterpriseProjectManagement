package com.enterprise.project_management.dto.auth;

import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

class RegisterRequestValidationTest {
  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @Test
  void rejectsInvalidEmail() {
    RegisterRequest request = validRequest();
    request.setEmail("not-an-email");
    assertTrue(validator.validate(request).stream().anyMatch(error -> error.getPropertyPath().toString().equals("email")));
  }

  @Test
  void rejectsWeakPassword() {
    RegisterRequest request = validRequest();
    request.setPassword("weak");
    assertTrue(validator.validate(request).stream().anyMatch(error -> error.getPropertyPath().toString().equals("password")));
  }

  @Test
  void rejectsMissingRequiredFields() {
    assertTrue(validator.validate(new RegisterRequest()).size() >= 5);
  }

  private RegisterRequest validRequest() {
    RegisterRequest request = new RegisterRequest();
    request.setName("Test User");
    request.setEmail("test@example.com");
    request.setPassword("StrongPassword1!");
    request.setEmployeeId("EMP-90");
    request.setDepartment("Engineering");
    return request;
  }
}
