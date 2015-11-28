package com.adobe.http.process.response;

import com.adobe.http.HttpUtils;
import com.adobe.http.models.HttpHeader;
import com.adobe.http.models.HttpResponseStatus;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.time.Instant;
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

    /**
     * Method to properly chuck and write a message to a channel via the buffer
     *
     * @param message A {@link String} containing the message
     * @param channel The {@link WritableByteChannel} target of the message
     * @throws IOException
     */
    protected void writeToChannel(String message, WritableByteChannel channel) throws IOException {
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

    /**
     * Helper method for writing a HTTP response status line
     *
     * @param channel The {@link WritableByteChannel} target to write to
     * @param status The {@link HttpResponseStatus} to be written
     *
     * @throws IOException
     */
    protected void writeStatus(WritableByteChannel channel, HttpResponseStatus status) throws IOException {
        this.writeToChannel(String.format("HTTP/1.1 %s %s\r\n", status.getCode(), status.getReason()), channel);
    }

    /**
     * Helper method for writing a group of headers
     *
     * @param channel The {@link WritableByteChannel} target of the headers
     * @param headers The {@link HttpHeader}s to be written
     *
     * @throws IOException
     */
    protected void writeHeaders(WritableByteChannel channel, HttpHeader... headers) throws IOException {
        writeHeaders(channel, Lists.newArrayList(headers));
    }

    /**
     * Helper method for writing a group of headers
     *
     * @param channel The {@link WritableByteChannel} target of the headers
     * @param headers A {@link List}&lt;{@link HttpHeader}&gt; of headers to be written
     *
     * @throws IOException
     */
    protected void writeHeaders(WritableByteChannel channel, List<HttpHeader> headers) throws IOException {
        List<HttpHeader> headersToWrite = Lists.newArrayList(headers);
        headersToWrite.addAll(this.headers);
        headersToWrite.add(new HttpHeader("Date", HttpUtils.convertInstantToString(Instant.now())));
        for(HttpHeader header : headersToWrite) {
            this.writeToChannel(String.format("%s: %s\r\n", header.getName(), header.getValue()), channel);
        }
        this.writeToChannel("\r\n", channel);
    }
}
