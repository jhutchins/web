package com.adobe.http.models;

import com.google.common.collect.Lists;
import org.junit.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by jhutchins on 11/27/15.
 */
public class HttpRequestTest {

    @Test
    public void shouldReturnInstant() {
        final HttpRequest request = HttpRequest.builder()
                .headers(Lists.newArrayList(new HttpHeader("If-Modified-Since", "Sat, 29 Oct 1994 21:43:31 GMT")))
                .build();
        final Instant expected = Instant.ofEpochSecond(783467011);
        assertThat(request.getIfModifiedSince()).isPresent().contains(expected);
    }

    @Test
    public void shouldNotReturnInstant() {
        final HttpRequest request = HttpRequest.builder()
                .headers(null)
                .build();
        assertThat(request.getIfModifiedSince()).isEmpty();
    }
}
