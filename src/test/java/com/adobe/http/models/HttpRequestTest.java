package com.adobe.http.models;

import com.adobe.http.models.headers.HttpHeader;
import com.adobe.http.models.headers.IfModifiedSince;
import com.google.common.collect.Lists;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by jhutchins on 11/27/15.
 */
public class HttpRequestTest {

    @Test
    public void shouldReturnInstant() {
        final String date = "Sat, 29 Oct 1994 21:43:31 GMT";
        final HttpRequest request = HttpRequest.builder()
                .headers(Lists.newArrayList(new HttpHeader("If-Modified-Since", date)))
                .build();
        final IfModifiedSince expected = new IfModifiedSince(date);
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
