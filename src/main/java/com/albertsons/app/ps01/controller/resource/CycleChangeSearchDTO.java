package com.albertsons.app.ps01.controller.resource;

import java.sql.Date;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.albertsons.app.ps01.validation.ChronologicalOrderDateConstraint;

import io.micrometer.core.lang.NonNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@ChronologicalOrderDateConstraint.List({
        @ChronologicalOrderDateConstraint(startDate = "startDate", endDate = "endDate", message = "End date should be later than start date.") })
public class CycleChangeSearchDTO {

    @NonNull
    @NotNull(message = "Division ID is required.")
    @NotEmpty(message = "Division ID is required.")
    private String divisionId;

    @NonNull
    private Date startDate;

    @NonNull
    private Date endDate;

}