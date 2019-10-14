package com.albertsons.app.ps01.domain.enums;

public enum ChangeStatusEnum {
    BASE(""),
    APPROVED("APPROVED"),
    SAVED("SAVED"),
    FOR_APPROVAL("FOR APPROVAL"),
    REJECTED("REJECTED");

    private String status;

    private ChangeStatusEnum(final String status) {
        this.status = status;
    }

    public String getChangeStatus() {
        return this.status;
    }

    public static ChangeStatusEnum getChangeStatusEnum(final String status) {
        for (ChangeStatusEnum cStatus : ChangeStatusEnum.values()) {
            if (cStatus.getChangeStatus().equals(status)) {
                return cStatus;
            }
        }

        return ChangeStatusEnum.BASE;
    }
}