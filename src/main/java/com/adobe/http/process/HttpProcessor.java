package com.adobe.http.process;

import com.adobe.http.parse.HttpMessage;

import java.nio.channels.SocketChannel;

/**
 * Created by jhutchins on 11/27/15.
 */
public interface HttpProcessor {

    ResponseWriter process(HttpMessage request, SocketChannel channel);

    String getType();
}
