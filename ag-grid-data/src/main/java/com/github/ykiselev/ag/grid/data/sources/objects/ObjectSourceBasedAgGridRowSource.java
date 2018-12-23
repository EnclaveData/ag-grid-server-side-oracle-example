package com.github.ykiselev.ag.grid.data.sources.objects;

import com.github.ykiselev.ag.grid.data.common.MapUtils;
import com.github.ykiselev.ag.grid.api.request.AgGridGetRowsRequest;
import com.github.ykiselev.ag.grid.api.request.SortModel;
import com.github.ykiselev.ag.grid.api.request.Sorting;
import com.github.ykiselev.ag.grid.api.response.AgGridGetRowsResponse;
import com.github.ykiselev.ag.grid.data.sources.AgGridRowSource;
import com.github.ykiselev.ag.grid.data.sources.Context;
import com.github.ykiselev.ag.grid.data.sources.DefaultRequestFilters;
import com.github.ykiselev.ag.grid.data.sources.RequestFilters;
import com.github.ykiselev.ag.grid.data.sources.objects.aggregation.Aggregation;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;
import static java.util.Objects.requireNonNull;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public final class ObjectSourceBasedAgGridRowSource<K, V> implements AgGridRowSource {

    private final ObjectSource<K, V> source;

    private final int batchSize;

    public ObjectSourceBasedAgGridRowSource(ObjectSource<K, V> source, int batchSize) {
        this.source = requireNonNull(source);
        this.batchSize = batchSize;
    }

    @Override
    public AgGridGetRowsResponse getRows(AgGridGetRowsRequest request) {
        return new ResponseBuilder(Context.create(request)).build();
    }

    private final class ResponseBuilder {

        private final Context context;

        ResponseBuilder(Context context) {
            this.context = requireNonNull(context);
        }

        AgGridGetRowsResponse build() {
            final Stream<V> rows = getFilteredRows(batchSize);
            final Stream<Map<String, Object>> result;
            if (context.isGrouping() || context.isPivot()) {
                result = Aggregation.groupBy(rows, context, source.getTypeInfo());
            } else {
                result = convert(rows);
            }
            return context.createResponse(limit(sort(result)));
        }

        private Stream<Map<String, Object>> convert(Stream<V> rows) {
            return rows.map(source.getTypeInfo().toMap());
        }

        private Stream<V> getFilteredRows(int batchSize) {
            final RequestFilters filters = DefaultRequestFilters.create(context.getRequest());
            final FilteredObjectSource<K, V> filteredSource =
                    source.filter(filters);
            // Source is free to ignore filters
            final Sets.SetView<String> toFilter =
                    Sets.difference(filters.getNames(), filteredSource.getFilteredNames());
            final Predicate<V> predicate = filter(toFilter, filters);
            // get keys and extract rows
            return StreamSupport.stream(
                    Iterables.partition(
                            filteredSource.getKeys(),
                            batchSize
                    ).spliterator(),
                    false
            ).map(filteredSource::getAll)
                    .flatMap(Collection::stream)
                    .filter(predicate);
        }

        private Predicate<V> filter(Set<String> columns, RequestFilters filters) {
            final Function<String, Predicate<V>> factory = col ->
                    Predicates.predicate(
                            source.getTypeInfo().getAttribute(col),
                            filters.getColumnFilter(col)
                    );
            return columns.stream()
                    .map(factory)
                    .reduce(Predicate::and)
                    .orElse(v -> true);
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
            Comparator<Map<String, Object>> comparator = Comparator.comparing(
                    MapUtils.extractValue(sortModel.getColId()),
                    nullsFirst(naturalOrder())
            );
            if (Sorting.ASC != sortModel.getSort()) {
                comparator = comparator.reversed();
            }
            return nullsFirst(comparator);
        }

        private List<Map<String, Object>> limit(Stream<Map<String, Object>> src) {
            return src.skip(context.getRequest().getStartRow())
                    .limit(context.getRequest().getEndRow() + 1)
                    .collect(Collectors.toList());
        }
    }
}