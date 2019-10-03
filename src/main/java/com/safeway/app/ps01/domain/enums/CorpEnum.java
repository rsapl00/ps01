package com.safeway.app.ps01.domain.enums;

public enum CorpEnum {

    DEFAULT_CORP("001");

    private String corpId;

    private CorpEnum (final String corpId) {
        this.corpId = corpId;
    }

    public String getCorpId() {
        return this.corpId;
    }
}