package com.cloudbees.assessment.api.controller;

import com.cloudbees.assessment.exception.CustomCloudBeesException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionController.class);

    @ExceptionHandler(CustomCloudBeesException.class)
    public ResponseEntity<String> handleCustomCloudBeesException(CustomCloudBeesException ex) {
        LOGGER.error(ex.getHttpStatus().getReasonPhrase(), ex.getErrorMessage());
        return ResponseEntity.status(ex.getHttpStatus()).body(ex.getErrorMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception ex) {
        LOGGER.error(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}
