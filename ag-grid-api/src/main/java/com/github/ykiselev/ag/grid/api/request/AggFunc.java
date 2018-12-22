package com.github.ykiselev.ag.grid.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public enum AggFunc {

    @JsonProperty("sum")
    SUM,
    @JsonProperty("avg")
    AVG,
    @JsonProperty("min")
    MIN,
    @JsonProperty("max")
    MAX;
}
