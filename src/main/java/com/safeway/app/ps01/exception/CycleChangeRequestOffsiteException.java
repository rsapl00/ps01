package com.safeway.app.ps01.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class CycleChangeRequestOffsiteException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public CycleChangeRequestOffsiteException(String message) {
        super(message);
    }

}