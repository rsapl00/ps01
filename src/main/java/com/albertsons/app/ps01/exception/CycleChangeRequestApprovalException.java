package com.albertsons.app.ps01.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class CycleChangeRequestApprovalException extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public CycleChangeRequestApprovalException(String message) {
        super(message);
    }

}