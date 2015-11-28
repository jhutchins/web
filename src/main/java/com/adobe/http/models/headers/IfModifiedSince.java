package com.adobe.http.models.headers;

import com.adobe.http.HttpUtils;

import java.time.Instant;

/**
 * Created by jhutchins on 11/28/15.
 */
public class IfModifiedSince extends HttpHeader {

    public static final String NAME = "If-Modified-Since";
    private final Instant instant;

    public IfModifiedSince(String value) {
        super(NAME, value);
        try {
            this.instant = HttpUtils.convertStringToInstant(value);
        } catch (Exception e) {
            throw new InvalidHeaderException(NAME, value);
        }
    }

    public boolean isModified(Instant lastModified) {
        return lastModified.isAfter(this.instant);
    }
}
