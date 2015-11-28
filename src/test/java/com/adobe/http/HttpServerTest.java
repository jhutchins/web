package com.adobe.http;

import com.adobe.http.process.HttpProcessorManager;
import org.junit.Before;

/**
 * Created by jhutchins on 11/27/15.
 */
public class HttpServerTest {

    private static final int PORT = 9876;
    private static final int POOL_SIZE = 10;
    private HttpServer server;

    @Before
    public void setup() {
        final HttpProcessorManager manager = new HttpProcessorManager();
        server = new HttpServer(PORT, POOL_SIZE, manager);
    }
}
