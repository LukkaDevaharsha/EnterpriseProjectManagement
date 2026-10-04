package com.enterprise.project_management.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

  @NotBlank(message = "Name is required")
  @Size(max = 100, message = "Name must be 100 characters or fewer")
  private String name;

  @NotBlank(message = "Email is required")
  @Email(message = "Enter a valid email address")
  @Size(max = 150, message = "Email must be 150 characters or fewer")
  private String email;

  @NotBlank(message = "Password is required")
  @Pattern(
      regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9\\s])\\S{10,72}$",
      message = "Password must be 10-72 characters and include uppercase, lowercase, number, and symbol")
  private String password;

  @NotBlank(message = "Employee ID is required")
  @Size(max = 50, message = "Employee ID must be 50 characters or fewer")
  private String employeeId;

  @NotBlank(message = "Department is required")
  @Size(max = 100, message = "Department must be 100 characters or fewer")
  private String department;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getEmployeeId() {
    return employeeId;
  }

  public void setEmployeeId(String employeeId) {
    this.employeeId = employeeId;
  }

  public String getDepartment() {
    return department;
  }

  public void setDepartment(String department) {
    this.department = department;
  }
}
