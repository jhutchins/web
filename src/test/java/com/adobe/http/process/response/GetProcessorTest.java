package com.adobe.http.process.response;

import com.adobe.http.HttpUtils;
import com.adobe.http.models.HttpRequest;
import com.adobe.http.models.headers.IfModifiedSince;
import com.adobe.http.models.headers.IfNoneMatch;
import com.adobe.http.process.EtagManager;
import com.adobe.http.process.GetProcessor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Created by jhutchins on 11/27/15.
 */
public class GetProcessorTest {
    private static final String BASE_DIR = "./test/public";
    private static final String ETAG = "923466024";
    public static final String DATA = "The data";

    @Mock
    private WritableByteChannel channel;
    @Mock
    private EtagManager manager;
    @Mock
    private HttpRequest request;

    private GetProcessor processor;
    private StringBuilder response;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);

        response = new StringBuilder();
        new File(BASE_DIR).mkdirs();
        processor = new GetProcessor(BASE_DIR, manager);

        when(channel.isOpen()).thenReturn(true);
        when(channel.write(any(ByteBuffer.class))).thenAnswer(invocation -> {
            ByteBuffer buffer = (ByteBuffer) invocation.getArguments()[0];
            int count = 0;
            while(buffer.hasRemaining()) {
                count++;
                response.append((char) buffer.get());
            }
            return count;
        });
        when(manager.retrieve(any(), any())).thenReturn(ETAG);
        when(request.getPath()).thenReturn("/a");
        when(request.getIfModifiedSince()).thenReturn(Optional.empty());
        when(request.getIfNoneMatch()).thenReturn(Optional.empty());
    }

    @After
    public void cleanup() throws Exception {
        delete(Paths.get(BASE_DIR).getParent().toFile());
    }

    private void delete(File file) {
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                delete(f);
            }
        }
        file.delete();
    }

    private void verifyNotFound() {
        assertThat(response.toString()).contains("404 Not Found").doesNotContain(DATA);
    }

    private void verifyNotModified() {
        assertThat(response.toString()).contains("304 Not Modified").doesNotContain(DATA).contains("ETag: " + ETAG);
    }

    private void verifyOk() {
        assertThat(response.toString()).contains("200 OK").contains(DATA).contains("ETag: " + ETAG);
    }

    @Test
    public void testFileNotFound() throws Exception {
        ResponseWriter writer = processor.process(request, channel);
        writer.write(channel);
        verifyNotFound();
    }

    @Test
    public void testFileExists() throws Exception {
        Files.write(Paths.get(BASE_DIR, "a"), DATA.getBytes());
        ResponseWriter writer = processor.process(request, channel);
        writer.write(channel);
        verifyOk();
    }

    @Test
    public void testNotFoundOutsideBaseDir() throws Exception {
        Files.write(Paths.get(BASE_DIR).getParent().resolve("a"), DATA.getBytes());
        when(request.getPath()).thenReturn("/../a");
        ResponseWriter writer = processor.process(request, channel);
        writer.write(channel);
        verifyNotFound();
    }

    @Test
    public void testResolvesRelativePathsInBaseDir() throws Exception {
        Paths.get(BASE_DIR, "subDir").toFile().mkdirs();
        Files.write(Paths.get(BASE_DIR, "a"), DATA.getBytes());
        when(request.getPath()).thenReturn("/subDir/../a");
        ResponseWriter writer = processor.process(request, channel);
        writer.write(channel);
        verifyOk();
    }

    @Test
    public void testReturnsNotModifiedWhenItShould() throws Exception {
        Files.write(Paths.get(BASE_DIR, "a"), DATA.getBytes());
        IfModifiedSince modifiedSince = new IfModifiedSince(
                HttpUtils.convertInstantToString(Instant.now().plusSeconds(5)));
        when(request.getIfModifiedSince()).thenReturn(Optional.of(modifiedSince));
        ResponseWriter writer = processor.process(request, channel);
        writer.write(channel);
        verifyNotModified();
    }

    @Test
    public void testDoesNotReturnsNotModifiedWhenItShouldNot() throws Exception {
        Files.write(Paths.get(BASE_DIR, "a"), DATA.getBytes());
        IfModifiedSince modifiedSince = new IfModifiedSince(
                HttpUtils.convertInstantToString(Instant.now().minusSeconds(5)));
        when(request.getIfModifiedSince()).thenReturn(Optional.of(modifiedSince));
        ResponseWriter writer = processor.process(request, channel);
        writer.write(channel);
        verifyOk();
    }

    @Test
    public void testDoesReturnNotModifiedWhenNoneMatchPresentWithMatch() throws Exception {
        Files.write(Paths.get(BASE_DIR, "a"), DATA.getBytes());
        when(request.getIfNoneMatch()).thenReturn(Optional.of(new IfNoneMatch("\"" + ETAG + "\"")));
        ResponseWriter writer = processor.process(request, channel);
        writer.write(channel);
        verifyNotModified();
    }

    @Test
    public void testDoesNotReturnNotModifiedWhenNoneMatchPresentWithoutMatch() throws Exception {
        Files.write(Paths.get(BASE_DIR, "a"), DATA.getBytes());
        when(request.getIfNoneMatch()).thenReturn(Optional.of(new IfNoneMatch("\"no match\"")));
        ResponseWriter writer = processor.process(request, channel);
        writer.write(channel);
        verifyOk();
    }

    @Test
    public void testDoesNotReturnNotModifiedWhenNoneMatchPresent() throws Exception {
        Files.write(Paths.get(BASE_DIR, "a"), DATA.getBytes());
        IfModifiedSince modifiedSince = new IfModifiedSince(
                HttpUtils.convertInstantToString(Instant.now().plusSeconds(5)));
        when(request.getIfModifiedSince()).thenReturn(Optional.of(modifiedSince));
        when(request.getIfNoneMatch()).thenReturn(Optional.of(new IfNoneMatch("\"no match\"")));
        ResponseWriter writer = processor.process(request, channel);
        writer.write(channel);
        verifyOk();
    }
}
