package com.safeway.app.ps01.domain;

import java.sql.Date;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.safeway.app.ps01.validation.ChronologicalOrderDateConstraint;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * 
 */
@Data
@NoArgsConstructor
@ChronologicalOrderDateConstraint.List( {
    @ChronologicalOrderDateConstraint(
        startDate = "runDate",
        endDate = "effectiveDate",
        message = "Effective date should be later than run date."
    )
})
@Entity
@Table(name="PSCYCREQ_TABLE")
public class CycleChangeRequest {

    @NonNull
    @Id
    @GeneratedValue
    @Column(name = "CYC_REQ_CHG_SK")
    private Long id;

    @NonNull
    @Column(name = "CORP_ID")
    private String corpId;

    @NotNull
    @NotBlank
    @NonNull
    @Column(name = "DIV_ID")
    private String divId;

    @NonNull
    @NotBlank
    @NotNull
    @Column(name = "RUN_DT")
    private Date runDate;

    @NonNull
    @NotNull
    @NotBlank
    @Column(name = "EFF_DT")
    private Date effectiveDate;

    @NonNull
    @Column(name = "CRT_TS")
    @CreatedDate
    private Timestamp createTimestamp;

    @NonNull
    @Column(name = "REQ_DAY_NM")
    private String requestDayName;

    @NonNull
    @Column(name = "RUN_NBR")
    private Integer runNumber;

    @NonNull
    @Column(name = "CYC_CHG_REQ_TYP_NM")
    private String cycleChangeRequestType;

    @NonNull
    @Column(name = "OFFSITE_IND")
    private String offsiteIndicator;

    @NonNull
    @Column(name = "CHG_STAT_NM")
    private String changeStatusName;

    @NonNull
    @Column(name = "CHG_TS")
    private Timestamp changeTimestamp;

    @NonNull
    @Column(name = "CMMT_TXT")
    private String comment;

    @NonNull
    @Column(name = "CRT_USR_ID")
    @CreatedBy
    private String createUserId;

    @NonNull
    @Column(name = "LST_UPD_USR_ID")
    @LastModifiedBy
    private String lastUpdatedUserId;

    @NonNull
    @Column(name = "EXPIRY_TS")
    private Timestamp expiryTimestamp;
    
}