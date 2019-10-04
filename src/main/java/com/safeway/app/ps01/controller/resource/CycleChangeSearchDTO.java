package com.safeway.app.ps01.controller.resource;

import java.sql.Date;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import io.micrometer.core.lang.NonNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CycleChangeSearchDTO {

    @NonNull
    @Valid
    @NotNull
    private String divisionId;

    @NonNull
    @Valid
    @NotNull
    private Date startDate;

    @NonNull
    @Valid
    @NotNull
    private Date endDate;

}