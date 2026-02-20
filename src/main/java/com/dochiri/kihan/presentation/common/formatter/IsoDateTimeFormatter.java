package com.dochiri.kihan.presentation.common.formatter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class IsoDateTimeFormatter {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public static String format(final LocalDateTime dateTime) {
        return dateTime.format(FORMATTER);
    }

}