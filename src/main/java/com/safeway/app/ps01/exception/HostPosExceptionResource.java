package com.safeway.app.ps01.exception;

import java.util.Date;

public class HostPosExceptionResource extends RuntimeException {

    /**
     *
     */
    private static final long serialVersionUID = 4667263228042370271L;
    
    private Date timestamp;
    private String message;
    private String details;

    public HostPosExceptionResource(Date timestamp, String message, String details) {
        super(message);
        this.timestamp = timestamp;
        this.message = message;
        this.details = details;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }

    public String getDetails() {
        return details;
    }
    
}