package com.safeway.app.ps01.domain.enums;

public enum DayEnum {

    SUNDAY("1"),
    MONDAY("2"),
    TUESDAY("3"),
    WEDNESDAY("4"),
    THURSDAY("5"),
    FRIDAY("6"),
    SATURDAY("7");

    private String dayNumber;

    private DayEnum (String dayNumber) {
        this.dayNumber = dayNumber;
    }

    public void setDayNumber(String dayNumber) {
        this.dayNumber = dayNumber;
    }

    public String getDayNumber () {
        return this.dayNumber;
    }

    public static DayEnum getDayEnum(String dayNumber) {

        for (DayEnum day : DayEnum.values()) {
            if (day.getDayNumber().equals(dayNumber)) {
                return day;
            }
        }

        // default Day
        return DayEnum.SUNDAY;
    }
}
