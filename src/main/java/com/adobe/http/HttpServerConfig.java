package com.adobe.http;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by jhutchins on 11/26/15.
 *
 * Representation of configuration options
 */
@Setter
@Getter
@ToString
public class HttpServerConfig{
    private String baseDir;
    private int port;
    private int poolSize;
}
