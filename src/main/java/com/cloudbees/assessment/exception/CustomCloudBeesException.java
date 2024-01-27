package com.cloudbees.assessment.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
public class CustomCloudBeesException extends Exception {

    private HttpStatus httpStatus;
    private String errorMessage;

}
