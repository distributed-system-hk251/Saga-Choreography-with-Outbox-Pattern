package com.distribute.products.expection;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.distribute.products.dto.response.ApiResponse;

import org.springframework.http.ResponseEntity;

@ControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(value = RuntimeException.class)
    public ResponseEntity<ApiResponse<String>> handleRuntimeException(RuntimeException ex) {
        System.err.println("RuntimeException caught: " + ex.getMessage());

        ApiResponse<String> apiResponse = new ApiResponse<String>(400, ex.getMessage(), null);
        return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

  
 }
