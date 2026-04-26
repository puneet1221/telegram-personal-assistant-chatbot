package com.project.personal_assistant.bot;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e) {
        log.error("unexpected error {}", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went Wrong !");
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRTE(RuntimeException rte) {
        log.error("Runtime Error", rte);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(rte.getMessage());
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<String> handleNPE(NullPointerException e) {
        log.error("Null pointer: ", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Null pointer exception occurred!");
    }
}
