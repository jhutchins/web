package com.adobe.http.models;

import com.adobe.http.HttpUtils;
import com.beust.jcommander.internal.Maps;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    private final Map<String, Object> parsedHeaders = Maps.newHashMap();

    public Optional<Instant> getIfModifiedSince() {
        final String name = "If-Modified-Since";
        Optional<Instant> time = (Optional<Instant>) parsedHeaders.get(name);
        if (time == null) {
            time = headers.stream()
                    .filter(header -> header.getName().equals(name))
                    .findFirst()
                    .map(HttpHeader::getValue)
                    .map(HttpUtils::covertStringToInstant);
            parsedHeaders.put(name, time);
        }
        return time;
    }
}
