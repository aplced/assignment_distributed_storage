package com.via.keyvalueservice.controllers;

import com.via.keyvalueservice.exceptions.KeyNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class KeyNotFoundAdvice {
    @ResponseBody
    @ExceptionHandler(KeyNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    String keyNotFoundHandler(KeyNotFoundException ex) {
        return ex.getMessage();
    }

}
