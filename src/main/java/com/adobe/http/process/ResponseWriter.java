package com.adobe.http.process;

import java.io.IOException;
import java.nio.channels.SocketChannel;

/**
 * Created by jhutchins on 11/27/15.
 */
public interface ResponseWriter {
    void write(SocketChannel channel) throws IOException;
}
