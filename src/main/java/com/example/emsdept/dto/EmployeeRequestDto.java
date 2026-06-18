package com.example.emsdept.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
@Getter
@Setter
public class EmployeeRequestDto {

    @NotBlank(message = "First name is required")
    @Size(max = 25, message = "First name cannot exceed 25 characters")
    @Pattern(
            regexp = "^[A-Za-z ]+$",
            message = "First name can contain only letters"
    )
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 25, message = "Last name cannot exceed 25 characters")
    @Pattern(
            regexp = "^[A-Za-z ]+$",
            message = "Last name can contain only letters"
    )
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 25, message = "Email cannot exceed 25 characters")
    private String email;

    @NotBlank(message = "Designation is required")
    @Size(max = 25, message = "Designation cannot exceed 25 characters")
    private String designation;

    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^\\+?[0-9]{10,15}$",
            message = "Invalid phone number"
    )
    private String phone;

    @NotNull(message = "Salary is required")
    @DecimalMin(
            value = "0.0",
            inclusive = false,
            message = "Salary must be greater than 0"
    )
    private BigDecimal salary;

    @NotNull(message = "Department is required")
    private Long departmentId;
}
