package com.adobe.http.process;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by jhutchins on 11/27/15.
 */
@Getter
@AllArgsConstructor
public class HttpResponseStatus {
    private final int code;
    private final String reason;
}
