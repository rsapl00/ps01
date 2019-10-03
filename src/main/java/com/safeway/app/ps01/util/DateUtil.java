package com.safeway.app.ps01.util;

import java.sql.Date;
import java.time.LocalDate;

import com.safeway.app.ps01.domain.enums.DayEnum;

public final class DateUtil {

    /**
     * Gets the nearest effective date of the given start date.
     * 
     * @param startDate     {@link Date} The date where to start searching.
     * @param effectiveDate {@link DayEnum} The date of the desired effective day.
     * @return {@link Date} The nearest effect
     */
    public static Date getEffectiveDate(Date startDate, DayEnum effectiveDate) {
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
}