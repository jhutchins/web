package com.adobe.http.process.response;

import com.adobe.http.models.HttpHeader;
import com.adobe.http.models.HttpResponseStatus;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;

/**
 * Created by jhutchins on 11/27/15.
 *
 * Class to facilitate the writting of response writers
 */
public abstract class AbstractResponseWriter implements ResponseWriter {

    private final ByteBuffer buffer = ByteBuffer.allocate(32);
    private final List<HttpHeader> headers = Lists.newArrayList(
            new HttpHeader("Server", "Test Server"));

    protected void writeToChannel(String message, SocketChannel channel) throws IOException {
        byte[] bytes = message.getBytes();
        int i = 0;
        this.buffer.clear();
        for (; i < bytes.length / this.buffer.limit(); i++) {
            this.buffer.put(bytes, i * this.buffer.limit(), this.buffer.limit());
            this.buffer.flip();
            while (this.buffer.hasRemaining()) {
                channel.write(this.buffer);
            }
            this.buffer.clear();
        }
        if (bytes.length % this.buffer.limit() != 0) {
            this.buffer.put(bytes, i * this.buffer.limit(), bytes.length % this.buffer.limit());
            this.buffer.flip();
            while (this.buffer.hasRemaining()) {
                channel.write(this.buffer);
            }
        }
    }

    protected void writeStatus(SocketChannel channel, HttpResponseStatus status) throws IOException {
        this.writeToChannel(String.format("HTTP/1.1 %s %s\r\n", status.getCode(), status.getReason()), channel);
    }

    protected void writeHeaders(SocketChannel channel, HttpHeader... headers) throws IOException {
        writeHeaders(channel, Lists.newArrayList(headers));
    }

    protected void writeHeaders(SocketChannel channel, List<HttpHeader> headers) throws IOException {
        List<HttpHeader> headersToWrite = Lists.newArrayList(headers);
        headersToWrite.addAll(this.headers);
        for(HttpHeader header : headersToWrite) {
            this.writeToChannel(String.format("%s: %s\r\n", header.getName(), header.getValue()), channel);
        }
        this.writeToChannel("\r\n", channel);
    }
}
