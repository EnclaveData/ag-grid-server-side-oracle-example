package com.ag.grid.enterprise.oracle.demo.data.mapsource;

import com.ag.grid.enterprise.oracle.demo.data.Context;
import com.ag.grid.enterprise.oracle.demo.data.DefaultRequestFilters;
import com.ag.grid.enterprise.oracle.demo.request.SortModel;
import com.ag.grid.enterprise.oracle.demo.response.AgGridGetRowsResponse;
import com.google.common.collect.Iterables;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Objects.requireNonNull;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
final class ResponseBuilder<K> {

    private final Context context;

    private final MapSource<K> source;

    ResponseBuilder(Context context, MapSource<K> source) {
        this.context = requireNonNull(context);
        this.source = requireNonNull(source);
    }

    AgGridGetRowsResponse build() {
        return context.createResponse(
                limit(sort(aggregate(getFilteredRows(5_000))))
        );
    }

    private Stream<Map<String, Object>> getFilteredRows(int batchSize) {
        final FilteredMapSource<K> filteredSource =
                source.filter(DefaultRequestFilters.create(context.getRequest()));
        // filter keys and extract rows
        return StreamSupport.stream(
                Iterables.partition(
                        filteredSource.getKeys(),
                        batchSize
                ).spliterator(),
                false
        ).map(filteredSource::getAll)
                .flatMap(Collection::stream);
    }

    private Stream<Map<String, Object>> aggregate(Stream<Map<String, Object>> src) {
        if (context.isGrouping() || context.isPivot()) {
            return Aggregation.transform(src, context);
        }
        return src;
    }

    private Stream<Map<String, Object>> sort(Stream<Map<String, Object>> src) {
        return context.getRequest()
                .getSortModel()
                .stream()
                .map(this::comparator)
                .reduce(Comparator::thenComparing)
                .map(src::sorted)
                .orElse(src);
    }

    private Comparator<Map<String, Object>> comparator(SortModel sortModel) {
        Comparator<Map<String, Object>> comparator = Comparator.comparing(MapUtils.extractValue(sortModel.getColId()));
        if (!"asc".equals(sortModel.getSort())) {
            comparator = comparator.reversed();
        }
        return Comparator.nullsFirst(comparator);
    }

    private List<Map<String, Object>> limit(Stream<Map<String, Object>> src) {
        return src.skip(context.getRequest().getStartRow())
                .limit(context.getRequest().getEndRow() + 1)
                .collect(Collectors.toList());
    }
}
