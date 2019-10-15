package com.albertsons.app.ps01.domain;

import java.sql.Date;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.albertsons.app.ps01.validation.ChronologicalOrderDateConstraint;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

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
@Entity
@Table(name = "PSCYCREQ_TABLE")
public class CycleChangeRequest {

    @NonNull
    @Id
    @GeneratedValue
    @Column(name = "CYC_REQ_CHG_SK")
    private Long id;

    @NonNull
    @Column(name = "CORP_ID")
    private String corpId;

    @NonNull
    @NotNull(message = "Division ID is required.")
    @NotBlank(message = "Division ID is required.")
    @Column(name = "DIV_ID")
    private String divId;

    @NonNull
    @Column(name = "RUN_DT")
    private Date runDate;

    @NonNull
    @Column(name = "EFF_DT")
    private Date effectiveDate;

    @NonNull
    @Column(name = "CRT_TS")
    @CreatedDate
    private Timestamp createTimestamp;

    @NonNull
    @Column(name = "RUN_NBR")
    private Integer runNumber;

    @NonNull
    @Column(name = "RUN_DY")
    private String runDayName;

    @NonNull
    @Column(name = "EFF_DY")
    private String effectiveDayName;

    @NonNull
    @Column(name = "CYC_CHG_REQ_TYP_NM")
    private String cycleChangeRequestType;

    @NonNull
    @NotBlank(message = "Offsite indicator is required. Accepted values are 0 and 1 only.")
    @NotNull(message = "Offsite indicator is required. Accepted values are 0 and 1 only.")
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
    @Column(name = "LST_UPD_TS")
    @LastModifiedDate
    private Timestamp lastUpdateTs;

    @NonNull
    @Column(name = "EXPIRY_TS")
    private Timestamp expiryTimestamp;

}