package com.adobe.http.process;

import com.adobe.http.parse.HttpHeader;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.List;

/**
 * Created by jhutchins on 11/27/15.
 */
public class StatusResponseWriter extends AbstractResponseWriter {

    private final HttpResponseStatus status;
    private final List<HttpHeader> headers;

    public StatusResponseWriter(HttpResponseStatus status, List<HttpHeader> headers) {
        this.status = status;
        this.headers = headers == null ? Collections.emptyList() : headers;
    }

    public StatusResponseWriter(HttpResponseStatus status, HttpHeader... headers) {
        this(status, Lists.newArrayList(headers));
    }

    @Override
    public void write(SocketChannel channel) throws IOException {
        this.writeStatus(channel, this.status);
        this.writeHeaders(channel, this.headers);
    }
}
