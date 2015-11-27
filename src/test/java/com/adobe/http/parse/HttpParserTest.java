package com.adobe.http.parse;

import static org.assertj.core.api.Assertions.*;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;

/**
 * Created by jhutchins on 11/25/15.
 */
public class HttpParserTest {

    private HttpParser parser;

    @Before
    public void setup() {
        parser = new HttpParser();
    }

    @Test
    public void shouldParseGet() {
        byte[] data =
                ("GET /path HTTP/1.1\r\n" +
                "Accept: application/json\r\n" +
                "\r\n").getBytes();
        HttpMessage expected = HttpMessage.builder()
                .path("/path")
                .method("GET")
                .version("HTTP/1.1")
                .headers(Lists.newArrayList(
                        new HttpHeader("Accept", "application/json")
                ))
                .build();

        for (int i = 0; i < data.length; i++) {
            parser.parse(data[i]);
        }
        assertThat(parser.getMessage()).isEqualToComparingFieldByField(expected);

    }

    @Test
    public void shouldParsePost() {
        byte[] data =
                ("POST /path HTTP/1.1\r\n" +
                        "Accept: application/json\r\n" +
                        "\r\n" +
                        "uri=something&parm=good").getBytes();
        HttpMessage expected = HttpMessage.builder()
                .path("/path")
                .method("POST")
                .version("HTTP/1.1")
                .headers(Lists.newArrayList(
                        new HttpHeader("Accept", "application/json")
                ))
                .build();

        for (int i = 0; i < data.length; i++) {
            parser.parse(data[i]);
        }
        assertThat(parser.getMessage()).isEqualToComparingFieldByField(expected);
    }
}
