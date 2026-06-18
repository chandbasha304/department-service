package com.example.emsdept.dto;


import jakarta.persistence.Column;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDto {



    private String firstName;

    private String lastName;

    @Column(unique = true)
    private String email;


    private String role;

    private String phone;

    private String status;
}