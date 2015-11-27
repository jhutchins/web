package com.adobe.http.parse;

import lombok.Builder;
import lombok.Getter;

import java.net.URI;
import java.util.List;

/**
 * Created by jhutchins on 11/25/15.
 */

@Getter
@Builder(builderClassName = "Builder")
public class HttpMessage {

    private final String method;
    private final String path;
    private final String version;
    private final List<HttpHeader> headers;
    private final String data;
}
