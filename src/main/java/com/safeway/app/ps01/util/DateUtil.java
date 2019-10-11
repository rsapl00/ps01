package com.safeway.app.ps01.util;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.safeway.app.ps01.domain.enums.BufferDayEnum;
import com.safeway.app.ps01.domain.enums.DayEnum;

public final class DateUtil {

    public static final String EXPIRATION_TS = "9999-12-31 00:00:00";
    public static final Integer BUFFER_DAYS = 7;

    /**
     * Gets the nearest effective date of the given start date.
     * 
     * @param startDate     {@link Date} The date where to start searching.
     * @param effectiveDate {@link DayEnum} The date of the desired effective day.
     * @return {@link Date} The nearest effect
     */
    public static Date getEffectiveDate(final Date startDate, final DayEnum effectiveDate) {
        return getEffectiveDate(startDate.toLocalDate(), effectiveDate);
    }

    /**
     * Gets the nearest effective date of the given start date.
     * 
     * @param startDate     {@link LocalDate} The date where to start searching.
     * @param effectiveDate {@link DayEnum} The date of the desired effective day.
     * @return {@link Date} The nearest effect
     */
    public static Date getEffectiveDate(final LocalDate startDate, final DayEnum effectiveDate) {

        // LocalDate start = startDate.plusDays(1); // NOTE: start date cannot be the effective date.
        LocalDate start = startDate;

        while (true) {

            if (DayEnum.getDayEnum(start.getDayOfWeek()).equals(effectiveDate)) {
                return java.sql.Date.valueOf(start);
            }

            start = start.plusDays(1);
        }
    }

    public static String getDayName(final Date date) {
        return DayEnum.getDayEnum(date.toLocalDate().getDayOfWeek()).getDayName();
    }

    public static Timestamp getExpiryTimestamp () {
        return Timestamp.valueOf(EXPIRATION_TS);
    }

    public static Timestamp expireNow() {
        return now();
    }

    public static Timestamp now() {
        return Timestamp.valueOf(LocalDateTime.now());
    }

    public static Date getBufferDate(final Date runDate, BufferDayEnum days) {
        LocalDate toProcess = runDate.toLocalDate();

        if (BufferDayEnum.PLUS_BUFFER == days) {
            return Date.valueOf(toProcess.plusDays(days.getDays()));
        } else {
            return Date.valueOf(toProcess.minusDays(days.getDays()));
        }
    }

    public static boolean isSameDate(Date date1, Date date2) {
        return date1.equals(date2);
    }

    public static boolean isBefore(Date from, Date to) {
        return from.toLocalDate().isBefore(to.toLocalDate());
    }

    public static boolean isAfter(Date from, Date to) {
        return from.toLocalDate().isAfter(to.toLocalDate());
    }

    public static boolean isEqual(Date date1, Date date2) {
        return date1.toLocalDate().isEqual(date2.toLocalDate());
    }
}