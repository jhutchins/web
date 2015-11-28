package com.adobe.http.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by jhutchins on 11/27/15.
 */
@Getter
@AllArgsConstructor
public class HttpResponseStatus {

    public static final HttpResponseStatus OK = new HttpResponseStatus(200, "OK");
    public static final HttpResponseStatus NOT_MODIFIED = new HttpResponseStatus(304, "Not Modified");
    public static final HttpResponseStatus BAD_REQUEST = new HttpResponseStatus(400, "Bad Request");
    public static final HttpResponseStatus NOT_FOUND = new HttpResponseStatus(404, "Not Found");
    public static final HttpResponseStatus NOT_ACCEPTABLE = new HttpResponseStatus(406, "Not Acceptable");

    private final int code;
    private final String reason;
}
