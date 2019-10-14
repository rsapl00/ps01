package com.albertsons.app.ps01.domain;

import java.io.Serializable;

import lombok.Data;
import lombok.NonNull;

@Data
public class CycleScheduleId implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @NonNull
    private String divId;

    @NonNull
    private String dayNum;

    public CycleScheduleId(){}

    public CycleScheduleId(String divId, String dayNum) {
        this.divId = divId;
        this.dayNum = dayNum;
    }
}