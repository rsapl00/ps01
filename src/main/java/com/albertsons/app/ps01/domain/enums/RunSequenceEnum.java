package com.albertsons.app.ps01.domain.enums;

public enum RunSequenceEnum {

    FIRST(1),
    SECOND(2);

    private int sequence;

    private RunSequenceEnum(final int sequence) {
        this.sequence = sequence;
    }

    public int getRunSequence() {
        return this.sequence;
    }

    public static RunSequenceEnum getRunSequenceEnum(int sequence) {

        for (RunSequenceEnum seq : RunSequenceEnum.values()) {
            if (seq.getRunSequence() == sequence) {
                return seq;
            }
        }

        return RunSequenceEnum.FIRST;
    }
}