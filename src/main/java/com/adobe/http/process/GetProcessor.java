package com.adobe.http.process;

import com.adobe.http.HttpUtils;
import com.adobe.http.models.headers.HttpHeader;
import com.adobe.http.models.HttpRequest;
import com.adobe.http.models.HttpResponseStatus;
import com.adobe.http.process.response.AbstractResponseWriter;
import com.adobe.http.process.response.ResponseWriter;
import com.adobe.http.process.response.StatusResponseWriter;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;

/**
 * Created by jhutchins on 11/27/15.
 *
 * Processor for handling GET requests.
 *
 * Return a writer that will respond with a 200 OK and the file (transfered using zero copy) if the file exists or
 * a 404 Not Found response if the file can't be found on the file system within the base directory.
 */
public class GetProcessor implements HttpProcessor {

    private static final ResponseWriter NOT_FOUND_WRITER = new StatusResponseWriter(HttpResponseStatus.NOT_FOUND);

    private final Path base;
    private final EtagManager manager;

    @Getter
    private final String type = "GET";

    public GetProcessor(String base, EtagManager manager) {
        this.base = Paths.get(base).normalize().toAbsolutePath();
        this.manager = manager;
    }

    /**
     * Process a GET request and retrieve the file if it exists and is within the directory being server.
     *
     * @param request The {@link HttpRequest} that is being serviced
     * @param channel The {@link WritableByteChannel} for the request
     *
     * @return A {@link ResponseWriter} that will write the appropriate response
     */
    @Override
    public ResponseWriter process(HttpRequest request, WritableByteChannel channel) {
        final Path target = this.base.resolve("." + request.getPath()).normalize().toAbsolutePath();
        final Instant lastModified = Instant.ofEpochMilli(target.toFile().lastModified());
        final String etag;

        try {
            etag = manager.retrieve(target, lastModified);
        } catch (Exception e) {
            return NOT_FOUND_WRITER;
        }

        // Check we're not breaking out of the root dir
        if (!target.startsWith(this.base)) {
            return NOT_FOUND_WRITER;
        } else if (!request.getIfNoneMatch().map(header -> header.noneMatch(etag)).orElse(true)) {
            return new StatusResponseWriter(
                    HttpResponseStatus.NOT_MODIFIED,
                    new HttpHeader("ETag", etag),
                    new HttpHeader("Last-Modified", HttpUtils.convertInstantToString(lastModified)));
        } else if (!request.getIfNoneMatch().isPresent()
                && !request.getIfModifiedSince().map(header -> header.isModified(lastModified)).orElse(true)) {
            return new StatusResponseWriter(
                    HttpResponseStatus.NOT_MODIFIED,
                    new HttpHeader("ETag", etag),
                    new HttpHeader("Last-Modified", HttpUtils.convertInstantToString(lastModified)));
        } else {
            return new GetResponseWriter(target, lastModified, etag);
        }
    }

    @AllArgsConstructor
    private static class GetResponseWriter extends AbstractResponseWriter {

        private final Path path;
        private final Instant lastModified;
        private final String etag;

        @Override
        public void write(WritableByteChannel channel) throws IOException {
            if (Files.exists(this.path)) {
                final File file = this.path.toFile();
                try (RandomAccessFile accessFile = new RandomAccessFile(file, "r");
                     FileChannel fileChannel = accessFile.getChannel()) {

                    this.writeStatus(channel, HttpResponseStatus.OK);
                    this.writeHeaders(channel,
                            new HttpHeader("Content-Length", "" + file.length()),
                            new HttpHeader("ETag", etag),
                            new HttpHeader("Last-Modified", HttpUtils.convertInstantToString(lastModified)));
                    fileChannel.transferTo(0, file.length(), channel);
                }
            } else {
                NOT_FOUND_WRITER.write(channel);
            }
        }
    }
}
