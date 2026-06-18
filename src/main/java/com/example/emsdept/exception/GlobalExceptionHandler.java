package com.example.emsdept.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> handleResponseStatusException(
            ResponseStatusException ex,
            HttpServletRequest request
    ) {

        ApiError error = new ApiError(
                ex.getStatusCode().value(),
                ex.getStatusCode().toString(),
                ex.getReason(),
                request.getRequestURI(),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(
                error,
                ex.getStatusCode()
        );
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiError> handleRuntimeException(
            RuntimeException ex,
            HttpServletRequest request
    ) {


        if(ex instanceof BadCredentialsException)
        {

            ApiError error = new ApiError(
                    HttpStatus.UNAUTHORIZED.value(),
                    "Invalid Credentials",
                    "Invalid Credentials",
                    request.getRequestURI(),
                    LocalDateTime.now()
            );

            return new ResponseEntity<>(
                    error,
                    HttpStatus.UNAUTHORIZED
            );

        }



        ApiError error = new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                ex.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(
                error,
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleException(
            Exception ex,
            HttpServletRequest request
    ) {

        ApiError error = new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Error",
                ex.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(
                error,
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }


    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ApiError> handleUserException(
            Exception ex,
            HttpServletRequest request
    ) {

        ApiError error = new ApiError(
                HttpStatus.UNAUTHORIZED.value(),
                "Error",
                ex.getMessage(),
                request.getRequestURI(),
                LocalDateTime.now()
        );

        return new ResponseEntity<>(
                error,
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }




    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error ->
                        error.getField() + ": " +
                                error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ApiError errorResponse = ApiError.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.toString())
                .message(message)
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.badRequest()
                .body(errorResponse);
    }


    @ExceptionHandler(
            org.springframework.web.method.annotation.HandlerMethodValidationException.class
    )
    public ResponseEntity<?> handleValidation(
            HandlerMethodValidationException ex) {

        ex.getAllErrors()
                .forEach(error -> System.out.println(error.getDefaultMessage()));

        return ResponseEntity.badRequest()
                .body(ex.getAllErrors());
    }
}