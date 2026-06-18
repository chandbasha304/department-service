package com.example.emsdept.exception;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class ApiError {

    private int status;

    private String error;

    private String message;

    private String path;

    private LocalDateTime timestamp;
}