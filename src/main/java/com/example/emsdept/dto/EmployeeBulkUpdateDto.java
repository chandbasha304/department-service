package com.example.emsdept.dto;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class EmployeeBulkUpdateDto {

    @NotNull
    private Long id;

    @NotBlank
    @Size(max = 25)
    @Pattern(regexp = "^[A-Za-z ]+$")
    private String firstName;

    @NotBlank
    @Size(max = 25)
    @Pattern(regexp = "^[A-Za-z ]+$")
    private String lastName;

    @NotBlank
    @Email
    @Size(max = 75)
    private String email;

    @NotBlank
    @Pattern(
            regexp = "^\\+?[0-9]{10,15}$",
            message = "Invalid phone number"
    )
    private String phone;

    @NotBlank
    @Size(max = 25)
    private String designation;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal salary;

    @NotNull
    private Long departmentId;
}