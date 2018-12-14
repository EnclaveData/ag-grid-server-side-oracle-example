package com.github.ykiselev.aggrid.sources.maps;

import com.github.ykiselev.aggrid.sources.AgGridRowSource;
import com.github.ykiselev.aggrid.sources.Context;
import com.github.ykiselev.aggrid.domain.request.AgGridGetRowsRequest;
import com.github.ykiselev.aggrid.domain.response.AgGridGetRowsResponse;

import static java.util.Objects.requireNonNull;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public final class MapSourceBasedAgGridRowSource<K> implements AgGridRowSource {

    private final MapSource<K> source;

    public MapSourceBasedAgGridRowSource(MapSource<K> source) {
        this.source = requireNonNull(source);
    }

    @Override
    public AgGridGetRowsResponse getRows(AgGridGetRowsRequest request) {
        return new ResponseBuilder<>(Context.create(request), source).build();
    }
}

