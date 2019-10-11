package com.safeway.app.ps01.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class CycleChangeRequestCancelException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public CycleChangeRequestCancelException(String message) {
        super(message);
    }

}