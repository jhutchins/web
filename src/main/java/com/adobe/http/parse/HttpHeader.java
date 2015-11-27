package com.adobe.http.parse;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Created by jhutchins on 11/25/15.
 */

@Getter
@Builder
@ToString
@EqualsAndHashCode
public class HttpHeader {
    private String name;
    private String version;
}
