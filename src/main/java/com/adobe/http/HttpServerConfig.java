package com.adobe.http;

import com.google.common.util.concurrent.AbstractExecutionThreadService;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by jhutchins on 11/26/15.
 */
@Setter
@Getter
@ToString
public class HttpServerConfig{
    private String base;
    private int port;
    private int poolSize;
}
