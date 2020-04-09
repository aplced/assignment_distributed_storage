package com.via.keyvalueservice.controllers.cluster;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class AlreadyInClusterAdvice {
    @ResponseBody
    @ExceptionHandler(AlreadyInClusterException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    String alreadyInClusterHandler(AlreadyInClusterException ex) {
        return ex.getMessage();
    }
}
