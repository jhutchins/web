package com.adobe.http;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Created by jhutchins on 11/27/15.
 */
public class HttpUtils {
    private static final DateTimeFormatter HTTP_DATE_FORMATTER = DateTimeFormatter
            .ofPattern("EEE, dd MMM yyyy HH:mm:ss z")
            .withZone(ZoneId.of("GMT"))
            .withLocale(Locale.US);

    public static String convertInstantToString(Instant instant) {
        return HTTP_DATE_FORMATTER.format(instant);
    }

    public static Instant covertStringToInstant(String date) {
        return HTTP_DATE_FORMATTER.parse(date, Instant::from);
    }
}
