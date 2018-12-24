package com.github.ykiselev.ag.grid.api.filter;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Yuriy Kiselev (uze@yandex.ru)
 * @since 24.12.2018
 */
public enum NumberFilterType {
    @JsonProperty("equals")
    EQUALS,
    @JsonProperty("notEqual")
    NOT_EQUAL,
    @JsonProperty("lessThan")
    LESS_THAN,
    @JsonProperty("lessThanOrEqual")
    LESS_THAN_OR_EQUAL,
    @JsonProperty("greaterThan")
    GREATER_THAN,
    @JsonProperty("greaterThanOrEqual")
    GREATER_THAN_OR_EQUAL,
    @JsonProperty("inRange")
    IN_RANGE

}
