package com.adobe.http.process;

import com.adobe.http.models.HttpHeader;
import com.adobe.http.models.HttpRequest;
import com.adobe.http.process.response.AbstractResponseWriter;
import com.adobe.http.models.HttpResponseStatus;
import com.adobe.http.process.response.ResponseWriter;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by jhutchins on 11/27/15.
 *
 * Processor for handling GET requests.
 *
 * Return a writer that will respond with a 200 OK and the file (transfered using zero copy) if the file exists or
 * a 404 Not Found response if the file can't be found on the file system
 */
@AllArgsConstructor
public class GetProcessor implements HttpProcessor {

    private final String base;

    @Getter
    private final String type = "GET";

    @Override
    public ResponseWriter process(HttpRequest request, SocketChannel channel) {
        return new GetResponseWriter(Paths.get(this.base, request.getPath()));
    }

    @AllArgsConstructor
    private static class GetResponseWriter extends AbstractResponseWriter {

        private final Path path;

        @Override
        public void write(SocketChannel channel) throws IOException {
            if (Files.exists(this.path)) {
                try (RandomAccessFile file = new RandomAccessFile(this.path.toFile(), "r")) {
                    FileChannel fileChannel = file.getChannel();
                    this.writeStatus(channel, HttpResponseStatus.OK);
                    this.writeHeaders(channel, new HttpHeader("Content-Length", "" + file.length()));
                    fileChannel.transferTo(0, file.length(), channel);
                }
            } else {
                this.writeStatus(channel, HttpResponseStatus.NOT_FOUND);
                this.writeHeaders(channel);
            }
        }
    }
}
