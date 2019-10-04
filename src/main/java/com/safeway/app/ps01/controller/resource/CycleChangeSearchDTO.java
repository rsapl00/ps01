package com.safeway.app.ps01.controller.resource;

import java.sql.Date;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.safeway.app.ps01.validation.CycleChangeSearchConstraint;

import io.micrometer.core.lang.NonNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@CycleChangeSearchConstraint.List( {
    @CycleChangeSearchConstraint(
        startDate = "startDate",
        endDate = "endDate",
        message = "End date should be later than start date."
    )
})
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