package com.adobe.http.process;

import com.adobe.http.models.HttpRequest;
import com.adobe.http.process.response.ResponseWriter;

import java.nio.channels.SocketChannel;

/**
 * Created by jhutchins on 11/27/15.
 */
public interface HttpProcessor {

    ResponseWriter process(HttpRequest request, SocketChannel channel);

    String getType();
}
