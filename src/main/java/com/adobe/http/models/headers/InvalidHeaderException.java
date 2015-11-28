package com.adobe.http.models.headers;

import lombok.Getter;

/**
 * Created by jhutchins on 11/28/15.
 */
@Getter
public class InvalidHeaderException extends RuntimeException {

    private final String name;
    private final String value;

    public InvalidHeaderException(String name, String value) {
        super(String.format("Invalid value [%s] for [%s]", value, name));
        this.name = name;
        this.value = value;

    }
}
