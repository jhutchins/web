package com.adobe.http;

import com.adobe.http.models.HttpRequest;
import com.adobe.http.models.HttpResponseStatus;
import com.adobe.http.parse.HttpParser;
import com.adobe.http.parse.HttpParsingException;
import com.adobe.http.process.HttpProcessorManager;
import com.adobe.http.process.response.StatusResponseWriter;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by jhutchins on 11/25/15.
 *
 * File based HTTP server
 */
@Slf4j
@RequiredArgsConstructor
public class HttpServer extends AbstractExecutionThreadService {

    public static final StatusResponseWriter BAD_REQUEST_RESPONSE_WRITER =
            new StatusResponseWriter(HttpResponseStatus.BAD_REQUEST);
    private final int port;
    private final int poolSize;
    private final HttpProcessorManager manager;

    private ServerSocketChannel serverChannel;
    private ExecutorService executor;

    /**
     * Process received request
     *
     * @param channel The {@link SocketChannel} of the incoming request
     */
    private void process(SocketChannel channel) {

        log.debug("Got connection {}", channel);

        final HttpParser parser = new HttpParser();
        final ByteBuffer buffer = ByteBuffer.allocate(32);

        try {
            // Read the data for the request
            int read = channel.read(buffer);
            while (read != -1) {
                buffer.flip();
                boolean done = false;
                while (buffer.hasRemaining() && !done) {
                    done = parser.parse(buffer.get());
                }
                if (done) {
                    break;
                }
                buffer.clear();
                read = channel.read(buffer);
            }

            try {
                // Parse the request
                HttpRequest request = parser.getMessage();

                // This is where a authentication would take place, if we were doing any

                manager.process(request, channel);
            } catch (HttpParsingException e) {
                // Return Bad Request in the case that parsing the request fails
                log.warn("Bad message", e);
                BAD_REQUEST_RESPONSE_WRITER.write(channel);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            // Make sure the channel gets closed
            try {
                channel.close();
            } catch (IOException e) {
                log.error("Failed to close SocketChannel", e);
            }
        }

    }

    /**
     * Start server by binding to the configured port and starting the thread pool
     */
    @Override
    protected void startUp() {
        try {
            this.serverChannel = ServerSocketChannel.open();
            this.serverChannel.socket().bind(new InetSocketAddress(this.port));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        executor = Executors.newFixedThreadPool(this.poolSize);
    }

    /**
     * While running accept incoming HTTP requests and process them
     * @throws Exception
     */
    @Override
    protected void run() throws Exception {
        log.info("Server started on {}", this.port);
        while (isRunning()) {
            final SocketChannel socketChannel = serverChannel.accept();
            executor.execute(() -> this.process(socketChannel));
        }
        log.info("Server stopped");
    }

    /**
     * On shutdown close the server channel and allow up to a minute for in progress requests to finish processing
     */
    @Override
    protected void shutDown() {
        try {
            serverChannel.close();
        } catch (IOException e) {
            log.error("Error shutting down server binding", e);
        }
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            log.warn("Terminating with pending jobs", e);
        }
    }
}
