package com.adobe.http.process;

import com.adobe.http.models.HttpHeader;
import com.adobe.http.models.HttpRequest;
import com.adobe.http.process.response.AbstractResponseWriter;
import com.adobe.http.models.HttpResponseStatus;
import com.adobe.http.process.response.ResponseWriter;
import com.adobe.http.process.response.StatusResponseWriter;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
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

    private static final ResponseWriter NOT_FOUND_WRITER = new StatusResponseWriter(HttpResponseStatus.NOT_FOUND);
    private final String base;

    @Getter
    private final String type = "GET";

    @Override
    public ResponseWriter process(HttpRequest request, WritableByteChannel channel) {
        Path target = Paths.get(this.base, request.getPath());
        // Check we're not breaking out of the root dir
        if (target.normalize().toAbsolutePath().startsWith(Paths.get(this.base).normalize().toAbsolutePath())) {
            return new GetResponseWriter(Paths.get(this.base, request.getPath()));
        } else {
            return NOT_FOUND_WRITER;
        }
    }

    @AllArgsConstructor
    private static class GetResponseWriter extends AbstractResponseWriter {

        private final Path path;

        @Override
        public void write(WritableByteChannel channel) throws IOException {
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
