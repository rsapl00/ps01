package com.albertsons.app.ps01.exception;

import java.util.Date;
import java.util.List;

public class HostPosExceptionResource {

    private Date timestamp;
    private List<String> messages;
    private String details;

    public HostPosExceptionResource(Date timestamp, List<String> messages, String details) {
        this.timestamp = timestamp;
        this.messages = messages;
        this.details = details;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public List<String> getMessages() {
        return messages;
    }

    public String getDetails() {
        return details;
    }
    
}