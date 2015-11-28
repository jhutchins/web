package com.adobe.http.models.headers;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by jhutchins on 11/28/15.
 */
public class IfNoneMatchTest {

    private static final String GOOD_VALUE = "xyzzy";
    private static final String BAD_VALUE = "bad";

    @Test
    public void testStar() {
        final IfNoneMatch header = new IfNoneMatch("*");
        assertThat(header.noneMatch(GOOD_VALUE)).isFalse();
    }

    @Test
    public void testSingleOptionMatch() {
        final IfNoneMatch header = new IfNoneMatch("\"xyzzy\"");
        assertThat(header.noneMatch(GOOD_VALUE)).isFalse();
    }

    @Test
    public void testMultipleOptionMatch() {
        final IfNoneMatch header = new IfNoneMatch("\"xyzzy\", \"r2d2xxxx\", \"c3piozzzz\"");
        assertThat(header.noneMatch(GOOD_VALUE)).isFalse();
    }

    @Test
    public void testSingleOptionNoMatch() {
        final IfNoneMatch header = new IfNoneMatch("\"xyzzy\"");
        assertThat(header.noneMatch(BAD_VALUE)).isTrue();
    }

    @Test
    public void testMultipleOptionNoMatch() {
        final IfNoneMatch header = new IfNoneMatch("\"xyzzy\", \"r2d2xxxx\", \"c3piozzzz\"");
        assertThat(header.noneMatch(BAD_VALUE)).isTrue();
    }

    @Test(expected = InvalidHeaderException.class)
    public void testThrowsErrorIfNull() {
        new IfNoneMatch(null);
    }

    @Test(expected = InvalidHeaderException.class)
    public void testThrowsErrorIfInvalid() {
        new IfNoneMatch("asdf");
    }
}
