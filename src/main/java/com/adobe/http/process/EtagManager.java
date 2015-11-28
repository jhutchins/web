package com.adobe.http.process;

import com.beust.jcommander.internal.Maps;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

/**
 * Created by jhutchins on 11/28/15.
 *
 * Manage the Etag (MD5 Hash) for files that have been previously served.
 */
@Slf4j
public class EtagManager {

    private final Map<String, Etag> etags = Maps.newHashMap();

    private String getKey(Path path) {
        return path.normalize().toAbsolutePath().toString();
    }

    private String makeEtag(Path path, Instant lastModified) {
        String etag;
        try {
            etag = Files.hash(path.toFile(), Hashing.md5()).toString();
        } catch (IOException e) {
            log.error("Error generating ETag", e);
            throw new RuntimeException(e);
        }
        etags.put(getKey(path), new Etag(lastModified, etag));
        return etag;
    }

    public String retrieve(Path path, Instant lastModified) {
        return Optional.ofNullable(etags.get(getKey(path)))
                .filter(tag -> !tag.getLastModified().isAfter(lastModified))
                .map(Etag::getHash)
                .orElseGet(() -> this.makeEtag(path, lastModified));
    }

    @Getter
    @AllArgsConstructor
    private static class Etag {
        private final Instant lastModified;
        private final String hash;
    }
}
