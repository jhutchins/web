package com.adobe.http;

import com.adobe.http.parse.HttpMessage;
import com.adobe.http.parse.HttpParser;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by jhutchins on 11/25/15.
 */
@Slf4j
@RequiredArgsConstructor
public class HttpServer extends AbstractExecutionThreadService {

    private final HttpServerConfig config;
    private ServerSocketChannel serverChannel;
    private ExecutorService executor;

    private void writeToChannel(String message, SocketChannel channel, ByteBuffer buffer) throws IOException {
        byte[] bytes = message.getBytes();
        int i = 0;
        buffer.clear();
        for (; i < bytes.length / buffer.limit(); i++) {
            buffer.put(bytes, i * buffer.limit(), buffer.limit());
            buffer.flip();
            while (buffer.hasRemaining()) {
                channel.write(buffer);
            }
            buffer.clear();
        }
        if (bytes.length % buffer.limit() != 0) {
            buffer.put(bytes, i * buffer.limit(), bytes.length % buffer.limit());
            buffer.flip();
            while (buffer.hasRemaining()) {
                channel.write(buffer);
            }
        }
    }

    private void process(SocketChannel channel) {

        log.debug("Got connection {}", channel);

        final HttpParser parser = new HttpParser();
        final ByteBuffer buffer = ByteBuffer.allocate(64);

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
            buffer.clear();

            Path path = Paths.get(config.getBase(), request.getPath());
            if (Files.exists(path)) {
                try (RandomAccessFile file = new RandomAccessFile(path.toFile(), "r")) {
                    FileChannel fileChannel = file.getChannel();
                    writeToChannel("HTTP/1.1 200 OK\r\nServer: Test Server\r\nContent-Length: " + file.length()
                            + "\r\n\r\n", channel, buffer);
                    fileChannel.transferTo(0, file.length(), channel);
                }
            } else {
                writeToChannel("HTTP/1.1 404 Not Found\r\nServer: Test Server\r\n\r\n", channel, buffer);
            }
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
            this.serverChannel.socket().bind(new InetSocketAddress(this.config.getPort()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        executor = Executors.newFixedThreadPool(config.getPoolSize());
    }

    @Override
    protected void run() throws Exception {
        log.info("Server started on {}", this.config.getPort());
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
