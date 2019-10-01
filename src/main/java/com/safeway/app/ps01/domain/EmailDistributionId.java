package com.safeway.app.ps01.domain;

import java.io.Serializable;

import lombok.Data;
import lombok.NonNull;

@Data
public class EmailDistributionId implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @NonNull
    private String corpId;

    @NonNull
    private String divId;

}