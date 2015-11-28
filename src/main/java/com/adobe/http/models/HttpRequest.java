package com.adobe.http.models;

import com.adobe.http.models.headers.HttpHeader;
import com.adobe.http.models.headers.IfModifiedSince;
import com.adobe.http.models.headers.IfNoneMatch;
import com.beust.jcommander.internal.Maps;
import lombok.Builder;
import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Created by jhutchins on 11/25/15.
 */

@Getter
@Builder(builderClassName = "Builder")
public class HttpRequest {
    private final String method;
    private final String path;
    private final String version;
    private final List<HttpHeader> headers;
    private final String data;

    private final Map<String, Optional<? extends HttpHeader>> parsedHeaders = Maps.newHashMap();

    private <T extends HttpHeader> Optional<T> getHeader(String name, Function<String, T> mapper) {
        Optional<T> value = (Optional<T>) parsedHeaders.get(name);
        if (value == null) {
            value = headers.stream()
                    .filter(header -> header.getName().equals(name))
                    .findFirst()
                    .map(HttpHeader::getValue)
                    .map(mapper);
            parsedHeaders.put(name, value);
        }
        return value;
    }

    public Optional<IfModifiedSince> getIfModifiedSince() {
        return getHeader(IfModifiedSince.NAME, IfModifiedSince::new);
    }

    public Optional<IfNoneMatch> getIfNoneMatch() {
        return getHeader(IfNoneMatch.NAME, IfNoneMatch::new);
    }

    public static class Builder {
        public HttpRequest build() {
            return new HttpRequest(method, path, version, headers != null ? headers : Collections.emptyList(), data);
        }
    }
}
