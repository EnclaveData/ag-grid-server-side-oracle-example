package com.github.ykiselev.aggrid.domain.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public enum Sorting {
    @JsonProperty("asc")
    ASC,
    @JsonProperty("desc")
    DESC;
}
