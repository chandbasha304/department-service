package com.example.emsdept.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {

    @NotBlank(message = "First name is required")
    @Size(max = 15, message = "First name cannot exceed 15 characters")
    @Pattern(
            regexp = "^[A-Za-z ]+$",
            message = "First name can contain only letters"
    )
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 15, message = "Last name cannot exceed 15 characters")
    @Pattern(
            regexp = "^[A-Za-z ]+$",
            message = "Last name can contain only letters"
    )
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 25, message = "Email cannot exceed 25 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,64}$",
            message = "Password must contain uppercase, lowercase, number and special character"
    )
    private String password;

    @NotBlank(message = "Phone number is required")
    @Pattern(
            regexp = "^\\+?[0-9]{10,15}$",
            message = "Invalid phone number"
    )
    private String phone;

    @NotBlank(message = "Role is required")
    @Pattern(
            regexp = "ADMIN|EMPLOYEE",
            message = "Role must be ADMIN or EMPLOYEE"
    )
    private String role;

    @NotBlank(message = "Status is required")
    @Pattern(
            regexp = "active|inactive",
            message = "Status must be active or inactive"
    )
    private String status;
}