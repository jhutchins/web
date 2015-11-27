package com.adobe.http.models;

import lombok.*;

/**
 * Created by jhutchins on 11/25/15.
 */

@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class HttpHeader {
    private String name;
    private String value;
}
