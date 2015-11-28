package com.adobe.http.models.headers;

import com.beust.jcommander.internal.Lists;

import java.util.List;

/**
 * Created by jhutchins on 11/28/15.
 */
public class IfNoneMatch extends HttpHeader {

    public static final String NAME = "If-None-Match";

    private final List<String> options = Lists.newLinkedList();
    private final boolean isStar;

    public IfNoneMatch(String value) {
        super(NAME, value);
        if (value == null) {
            throw new InvalidHeaderException(NAME, value);
        }
        if (value.equals("*")) {
            this.isStar = true;
        } else {
            this.isStar = false;
            final String[] parts = value.split(", ");
            for (int i = 0; i < parts.length; i++) {
                final String option = parts[i];
                if (!option.startsWith("\"") || !option.endsWith("\"")) {
                    throw new InvalidHeaderException(NAME, value);
                }
                this.options.add(option.substring(1, option.length() - 1));
            }
        }
    }

    public boolean noneMatch(String value) {
        return !this.isStar && this.options.stream().noneMatch(value::equals);
    }
}
