package com.adobe.http;

import com.adobe.http.process.GetProcessor;
import com.adobe.http.process.HttpProcessorManager;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.fasterxml.jackson.jr.ob.JSON;

import java.io.FileInputStream;

/**
 * Created by jhutchins on 11/26/15.
 */
public class Main {

    public static void main(String[] argv) throws Exception {
        final Options options = new Options();
        new JCommander(options, argv);

        final HttpServerConfig config = JSON.std.beanFrom(HttpServerConfig.class, new FileInputStream(options.config));
        final HttpProcessorManager manager = new HttpProcessorManager();
        manager.addProcessor(new GetProcessor(config.getBase()));
        final HttpServer server = new HttpServer(config.getPort(), config.getPoolSize(), manager);

        server.startAsync();
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                server.stopAsync();
            }
        });
        server.awaitTerminated();
    }

    private static class Options {
        @Parameter(names = {"--config"}, description = "Path to the config file")
        private String config;
    }
}
