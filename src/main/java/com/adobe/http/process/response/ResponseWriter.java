package com.adobe.http.process.response;

import java.io.IOException;
import java.nio.channels.WritableByteChannel;

/**
 * Created by jhutchins on 11/27/15.
 */
public interface ResponseWriter {
    void write(WritableByteChannel channel) throws IOException;
}
