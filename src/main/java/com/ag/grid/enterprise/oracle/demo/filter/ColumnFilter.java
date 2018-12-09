package com.ag.grid.enterprise.oracle.demo.filter;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "filterType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = NumberColumnFilter.class, name = "number"),
        @JsonSubTypes.Type(value = SetColumnFilter.class, name = "set"),
        @JsonSubTypes.Type(value = TextColumnFilter.class, name = "text")
})
public abstract class ColumnFilter {

    String filterType;
}