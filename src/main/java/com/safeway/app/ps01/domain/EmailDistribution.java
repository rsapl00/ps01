package com.safeway.app.ps01.domain;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import lombok.NonNull;

/**
 * 
 * TODO: Implement AutoAware for Audit fields like Created By
 * 
 */
@Entity
@Table (name = "PSDIVDST_TABLE")
@IdClass(EmailDistributionId.class)
public class EmailDistribution {

    @NonNull
    @Id
    @Column(name="CORP_ID")
    private String corpId;

    @NonNull
    @Id
    @Column(name="DIV_ID")
    private String divId;

    @NonNull
    @Column(name="EMAIL_ADDR_TXT")
    private String email;

    @NonNull
    @Column(name="CRT_TS")
    @CreatedDate
    private Timestamp createTimestamp;

    @NonNull
    @Column(name="CRT_USR_ID")
    @CreatedBy
    private String createUserId;
    
    @NonNull
    @Column(name="LST_UPD_TS")
    @LastModifiedDate
    private Timestamp lastUpdateTs;

    @NonNull
    @Column(name="LST_UPD_USR_ID")
    @LastModifiedBy
    private String lastUpdateUserId;
}