package com.ag.grid.enterprise.oracle.demo.data.objectsource;

import com.ag.grid.enterprise.oracle.demo.data.AgGridRowSource;
import com.ag.grid.enterprise.oracle.demo.data.Context;
import com.ag.grid.enterprise.oracle.demo.request.AgGridGetRowsRequest;
import com.ag.grid.enterprise.oracle.demo.response.AgGridGetRowsResponse;

import static java.util.Objects.requireNonNull;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public final class ObjectSourceBasedAgGridRowSource<K, V> implements AgGridRowSource {

    private final ObjectSource<K, V> source;

    public ObjectSourceBasedAgGridRowSource(ObjectSource<K, V> source) {
        this.source = requireNonNull(source);
    }

    @Override
    public AgGridGetRowsResponse getRows(AgGridGetRowsRequest request) {
        return new ResponseBuilder<>(Context.create(request), source).build();
    }
}

