package com.albertsons.app.ps01.controller.resource;

import java.sql.Date;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.albertsons.app.ps01.domain.CycleChangeRequest;
import com.albertsons.app.ps01.validation.ChronologicalOrderDateConstraint;
import com.albertsons.app.ps01.validation.RundateConstraint;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * 
 */
@Data
@NoArgsConstructor
@ChronologicalOrderDateConstraint.List({
        @ChronologicalOrderDateConstraint(startDate = "runDate", endDate = "effectiveDate", message = "Effective date should be later than run date.") })
public class CycleChangeRequestDTO {

    @NonNull
    @NotNull(message = "Division ID is required.")
    @NotBlank(message = "Division ID is required.")
    private String divId;

    @NonNull
    @RundateConstraint
    private Date runDate;

    @NonNull
    private Date effectiveDate;

    @NonNull
    @NotBlank(message = "Offsite indicator is required. Accepted values are 0 and 1 only.")
    @NotNull(message = "Offsite indicator is required. Accepted values are 0 and 1 only.")
    private String offsiteIndicator;

    public CycleChangeRequest getCycleChangeRequest() {
        CycleChangeRequest request = new CycleChangeRequest();
        request.setDivId(this.divId);
        request.setRunDate(this.runDate);
        request.setEffectiveDate(this.effectiveDate);
        request.setOffsiteIndicator(this.offsiteIndicator);
        
        return request;
    }
}