package com.adobe.http.process;

import com.adobe.http.models.HttpHeader;
import com.adobe.http.models.HttpRequest;
import com.adobe.http.models.HttpResponseStatus;
import com.adobe.http.process.response.ResponseWriter;
import com.adobe.http.process.response.StatusResponseWriter;
import com.beust.jcommander.internal.Maps;
import com.google.common.base.Joiner;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.Optional;

/**
 * Created by jhutchins on 11/27/15.
 *
 * Stored configured processors and delegate to them as appropriate.
 */
public class HttpProcessorManager {
    private final Map<String, HttpProcessor> processors = Maps.newHashMap();

    public void addProcessor(HttpProcessor processor) {
        processors.put(processor.getType(), processor);
    }

    /**
     * Process the request with the appropriate delegate processor.
     *
     * Write a 406 Not Acceptable response in the case that no delegate exists
     *
     * @param request The {@link HttpRequest} to be processed
     * @param channel The {@link SocketChannel} to write the response to
     *
     * @throws IOException
     */
    public void process(HttpRequest request, SocketChannel channel) throws IOException {
        Optional.ofNullable(processors.get(request.getMethod()))
                .map(processor -> processor.process(request, channel))
                .orElseGet(this::getNotAcceptableStatusResponseWriter)
                .write(channel);
    }

    private ResponseWriter getNotAcceptableStatusResponseWriter() {
        return new StatusResponseWriter(
                HttpResponseStatus.NOT_ACCEPTABLE,
                new HttpHeader("Allow", Joiner.on(", ").join(this.processors.keySet())));
    }
}
