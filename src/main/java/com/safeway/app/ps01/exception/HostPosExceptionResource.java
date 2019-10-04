package com.safeway.app.ps01.exception;

import java.util.Date;
import java.util.List;

public class HostPosExceptionResource {

    /**
     *
     */
    private static final long serialVersionUID = 4667263228042370271L;
    
    private Date timestamp;
    private String message;
    private List<String> details;

    public HostPosExceptionResource(Date timestamp, String message, List<String> details) {
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

    public List<String> getDetails() {
        return details;
    }
    
}