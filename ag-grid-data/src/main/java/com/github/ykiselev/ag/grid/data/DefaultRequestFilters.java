package com.github.ykiselev.ag.grid.data;

import com.github.ykiselev.ag.grid.api.filter.ColumnFilter;
import com.github.ykiselev.ag.grid.api.filter.GroupKey;
import com.github.ykiselev.ag.grid.api.request.AgGridGetRowsRequest;
import com.github.ykiselev.ag.grid.api.request.ColumnVO;
import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public final class DefaultRequestFilters implements RequestFilters {

    private final Map<String, ColumnFilter> filters;

    public DefaultRequestFilters(Map<String, ColumnFilter> filters) {
        this.filters = ImmutableMap.copyOf(filters);
    }

    @Override
    public Set<String> getNames() {
        return filters.keySet();
    }

    @Override
    public ColumnFilter getFilter(String field) {
        return filters.get(field);
    }

    public static RequestFilters create(AgGridGetRowsRequest request) {
        final Map<String, ColumnFilter> map = new HashMap<>(request.getFilterModel());
        final List<String> keys = request.getGroupKeys();
        final List<ColumnVO> columns = request.getRowGroupCols();
        IntStream.range(0, Math.min(keys.size(), columns.size()))
                .forEach(k ->
                        map.putIfAbsent(
                                columns.get(k).getField(),
                                new GroupKey(keys.get(k))
                        )
                );
        return new DefaultRequestFilters(map);
    }
}
