package com.adobe.http.process;

import com.adobe.http.parse.HttpHeader;
import com.adobe.http.parse.HttpMessage;
import com.beust.jcommander.internal.Maps;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Optional;

/**
 * Created by jhutchins on 11/27/15.
 */
public class HttpProcessorManager {
    private final Map<String, HttpProcessor> processors = Maps.newHashMap();

    public void addProcessor(HttpProcessor processor) {
        processors.put(processor.getType(), processor);
    }

    public void process(HttpMessage request, SocketChannel channel) throws IOException {
        Optional.ofNullable(processors.get(request.getMethod()))
                .map(processor -> processor.process(request, channel))
                .orElseGet(this::getNotAcceptableStatusResponseWriter)
                .write(channel);
    }

    private ResponseWriter getNotAcceptableStatusResponseWriter() {
        return new StatusResponseWriter(
                new HttpResponseStatus(406, "Not Acceptable"),
                new HttpHeader("Allow", Joiner.on(", ").join(this.processors.keySet())));
    }
}
