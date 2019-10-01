package com.safeway.app.ps01.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import lombok.Data;
import lombok.NonNull;

@Entity
@Table(name = "PSCYCSCH")
@Data
public class CycleSchedule {

    @NonNull
    @Column(name = "DIV")
    private String division;

    @NonNull
    @Column(name = "DAY_NUM")
    private String dayNum;

    @NonNull
    @Column(name = "POS_RUN_SW")
    private String posRunSw;

    @NonNull
    @Column(name = "RUN_1_EFF_DAY_NBR")
    private String runOneEffectiveDayNbr;

    @NonNull
    @Column(name = "RUN_2_EFF_DAY_NBR")
    private String runTwoEffectiveDayNbr;

    @NonNull
    @Column(name = "DEF_1_EFF_DAY_NBR")
    private String defOneEffectiveDayNbr;

    @NonNull
    @Column(name = "DEF_2_EFF_DAY_NBR")
    private String defTwoEffectiveDayNbr;

    @NonNull
    @Column(name = "RUN_1_CHG_OS_IND")
    private String runOneChangeOsInd;

    @NonNull
    @Column(name = "RUN_2_CHG_OS_IND")
    private String runTwoChangeOsInd;

    @NonNull
    @Column(name = "DFLT_RUN_1_OS_IND")
    private String defaultRunOneOsInd;

    @NonNull
    @Column(name = "DFLT_RUN_2_OS_IND")
    private String defaultRunTwoOsInd;

}