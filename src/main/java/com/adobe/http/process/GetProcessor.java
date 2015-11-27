package com.adobe.http.process;

import com.adobe.http.parse.HttpHeader;
import com.adobe.http.parse.HttpMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Created by jhutchins on 11/27/15.
 */
@AllArgsConstructor
public class GetProcessor implements HttpProcessor {

    private final String base;

    @Getter
    private final String type = "GET";

    @Override
    public ResponseWriter process(HttpMessage request, SocketChannel channel) {
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
                    this.writeStatus(channel, new HttpResponseStatus(200, "OK"));
                    this.writeHeaders(channel, new HttpHeader("Content-Length", "" + file.length()));
                    fileChannel.transferTo(0, file.length(), channel);
                }
            } else {
                this.writeStatus(channel, new HttpResponseStatus(404, "Not Found"));
                this.writeHeaders(channel);
            }
        }
    }
}
