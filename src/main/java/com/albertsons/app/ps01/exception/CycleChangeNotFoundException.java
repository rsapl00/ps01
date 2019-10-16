package com.albertsons.app.ps01.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CycleChangeNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public CycleChangeNotFoundException(String message) {
        super(message);
    }

    public CycleChangeNotFoundException() {
        super("Cycle Change Request not found.");
    }

}
