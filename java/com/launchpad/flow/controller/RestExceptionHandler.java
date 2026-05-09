package com.launchpad.flow.controller;

import com.launchpad.flow.service.AppException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {
    @ExceptionHandler(AppException.class)
    ResponseEntity<Map<String, Object>> appException(AppException exception) {
        return ResponseEntity
            .status(exception.getStatus())
            .body(error(exception.getMessage(), exception.getStatus().value()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, Object>> validation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult()
            .getFieldErrors()
            .stream()
            .findFirst()
            .map(FieldError::getDefaultMessage)
            .orElse("Invalid request");
        return ResponseEntity.badRequest().body(error(message, 400));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Map<String, Object>> generic(Exception exception) {
        return ResponseEntity
            .internalServerError()
            .body(error(exception.getMessage(), 500));
    }

    private Map<String, Object> error(String message, int status) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", message == null ? "Unexpected error" : message);
        body.put("status", status);
        return body;
    }
}
