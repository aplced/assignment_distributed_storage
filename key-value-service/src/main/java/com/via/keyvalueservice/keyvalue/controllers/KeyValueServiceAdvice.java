package com.via.keyvalueservice.keyvalue.controllers;

import com.via.keyvalueservice.cluster.ClusterNodeUpdateCollisionException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class KeyValueServiceAdvice {
    @ResponseBody
    @ExceptionHandler(KeyValueServiceException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    String keyNotFoundHandler(KeyValueServiceException ex) {
        return ex.getMessage();
    }

    @ResponseBody
    @ExceptionHandler(ClusterNodeUpdateCollisionException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    String clusterNodeUpdateCollisionAdviceHandler(ClusterNodeUpdateCollisionException ex) {
        return ex.getMessage();
    }
}
