package com.ag.grid.enterprise.oracle.demo.data.objectsource;

import com.ag.grid.enterprise.oracle.demo.data.Context;
import com.ag.grid.enterprise.oracle.demo.data.DefaultRequestFilters;
import com.ag.grid.enterprise.oracle.demo.data.mapsource.AggregationResult;
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
final class ResponseBuilder<K, V> {

    private final Context context;

    private final ObjectSource<K, V> source;

    ResponseBuilder(Context context, ObjectSource<K, V> source) {
        this.context = requireNonNull(context);
        this.source = requireNonNull(source);
    }

    AgGridGetRowsResponse build() {
        final Stream<V> rows = getFilteredRows(5_000);
        final Stream<Map<String, Object>> result;
        if (context.isGrouping() || context.isPivot()) {
            result = new AggregationResult(context,
                    rows.collect(Aggregation.create(context, source))).parse();
        } else {
            result = convert(rows);
        }
        return context.createResponse(limit(sort(result)));
    }

    private Stream<Map<String, Object>> convert(Stream<V> rows) {
        throw new UnsupportedOperationException("not implemented");
    }

    private Stream<V> getFilteredRows(int batchSize) {
        final FilteredObjectSource<K, V> filteredSource =
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
