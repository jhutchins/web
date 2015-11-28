package com.adobe.http.models.headers;

import org.junit.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by jhutchins on 11/28/15.
 */
public class IfModifiedSinceTest {

    private static final String DATE = "Sat, 29 Oct 1994 21:43:31 GMT";
    private static final Instant INSTANT = Instant.ofEpochSecond(783467011);

    @Test
    public void testTrueIfAfter() {
        final IfModifiedSince header = new IfModifiedSince(DATE);
        assertThat(header.isModified(INSTANT.plusSeconds(5))).isTrue();
    }

    @Test
    public void testTrueIfEqual() {
        final IfModifiedSince header = new IfModifiedSince(DATE);
        assertThat(header.isModified(INSTANT)).isFalse();
    }

    @Test
    public void testFalseIfBefore() {
        final IfModifiedSince header = new IfModifiedSince(DATE);
        assertThat(header.isModified(INSTANT.minusSeconds(5))).isFalse();
    }

    @Test(expected = InvalidHeaderException.class)
    public void testThrowsErrorIfNull() {
        new IfModifiedSince(null);
    }

    @Test(expected = InvalidHeaderException.class)
    public void testThrowsErrorIfInvalid() {
        new IfModifiedSince("asdf");
    }
}
