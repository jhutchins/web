package com.adobe.http.parse;

import com.adobe.http.models.headers.HttpHeader;
import com.adobe.http.models.HttpRequest;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created by jhutchins on 11/25/15.
 */
public class HttpParser {

    private enum State {
        METHOD,
        PATH,
        PROTOCOL,
        HEADERS,
        DONE
    }

    private State state = State.METHOD;
    private StringBuilder current = new StringBuilder();
    private final HttpRequest.Builder builder = HttpRequest.builder();
    private final List<HttpHeader> headers = Lists.newLinkedList();

    public HttpRequest getMessage() {
        return builder.build();
    }

    public boolean parse(byte b) {
        return parse((char) b);
    }

    public boolean parse(char b) {

        switch(state) {
            case METHOD:
                if (b == ' ') {
                    builder.method(current.toString());
                    current = new StringBuilder();
                    this.state = State.PATH;
                } else {
                    current.append(b);
                }
                break;
            case PATH:
                if (b == ' ') {
                    builder.path(current.toString());
                    current = new StringBuilder();
                    this.state = State.PROTOCOL;
                } else {
                    current.append(b);
                }
                break;
            case PROTOCOL:
                if (b == '\n') {
                    builder.version(current.toString());
                    current = new StringBuilder();
                    this.state = State.HEADERS;
                } else if (b != '\r') {
                    current.append(b);
                }
                break;
            case HEADERS:
                if (b == '\n') {
                    String str = current.toString();
                    if (str.isEmpty()) {
                        builder.headers(headers);
                        this.state = State.DONE;
                    } else {
                        String[] parts = str.split(":", 2);
                        if (parts.length != 2) {
                            throw new HttpParsingException();
                        }
                        headers.add(new HttpHeader(parts[0].trim(), parts[1].trim()));
                        current = new StringBuilder();
                    }
                } else if (b != '\r') {
                    current.append(b);
                }
                break;
            case DONE:
                break;
        }

        return this.state == State.DONE;
    }
}
