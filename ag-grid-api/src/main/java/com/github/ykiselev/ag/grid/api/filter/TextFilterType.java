package com.github.ykiselev.ag.grid.api.filter;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Yuriy Kiselev (uze@yandex.ru)
 * @since 24.12.2018
 */
public enum TextFilterType {
    @JsonProperty("equals")
    EQUALS,
    @JsonProperty("notEqual")
    NOT_EQUAL,
    @JsonProperty("contains")
    CONTAINS,
    @JsonProperty("notContains")
    NOT_CONTAINS,
    @JsonProperty("startsWith")
    STARTS_WITH,
    @JsonProperty("endsWith")
    ENDS_WITH;
}
