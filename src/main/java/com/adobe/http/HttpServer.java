package com.adobe.http;

import com.adobe.http.parse.HttpMessage;
import com.adobe.http.parse.HttpParser;
import com.adobe.http.process.HttpProcessorManager;
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
 */
@Slf4j
@RequiredArgsConstructor
public class HttpServer extends AbstractExecutionThreadService {

    private final int port;
    private final int poolSize;
    private final HttpProcessorManager manager;

    private ServerSocketChannel serverChannel;
    private ExecutorService executor;

    private void process(SocketChannel channel) {

        log.debug("Got connection {}", channel);

        final HttpParser parser = new HttpParser();
        final ByteBuffer buffer = ByteBuffer.allocate(32);

        try {
            int read = channel.read(buffer);
            while (read != -1) {
                buffer.flip();
                boolean done = false;
                while (buffer.hasRemaining()) {
                    done = parser.parse(buffer.get());
                }
                if (done) {
                    break;
                }
                buffer.clear();
                read = channel.read(buffer);
            }

            HttpMessage request = parser.getMessage();
            manager.process(request, channel);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                channel.close();
            } catch (IOException e) {
                log.error("Failed to close SocketChannel", e);
            }
        }

    }

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

    @Override
    protected void run() throws Exception {
        log.info("Server started on {}", this.port);
        while (isRunning()) {
            final SocketChannel socketChannel = serverChannel.accept();
            executor.execute(() -> this.process(socketChannel));
        }
        log.info("Server stopped");
    }

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
