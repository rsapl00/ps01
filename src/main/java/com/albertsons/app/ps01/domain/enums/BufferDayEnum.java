package com.albertsons.app.ps01.domain.enums;

public enum BufferDayEnum {
    PLUS_BUFFER(8),
    MINUS_BUFFER(8);

    private Integer days;

    private BufferDayEnum(final Integer days) {
        this.days = days;
    }

    public Integer getDays() {
        return this.days;
    }

    public static BufferDayEnum getBufferDay(Integer days) {
        for (BufferDayEnum day : BufferDayEnum.values()) {
            if (day.getDays().equals(days)) {
                return day;
            }
        }

        return PLUS_BUFFER;
    }
}