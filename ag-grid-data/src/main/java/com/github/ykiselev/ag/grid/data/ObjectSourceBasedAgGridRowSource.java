package com.github.ykiselev.ag.grid.data;

import com.github.ykiselev.ag.grid.api.request.AgGridGetRowsRequest;
import com.github.ykiselev.ag.grid.api.request.SortModel;
import com.github.ykiselev.ag.grid.api.request.Sorting;
import com.github.ykiselev.ag.grid.api.response.AgGridGetRowsResponse;
import com.github.ykiselev.ag.grid.data.aggregation.Aggregation;
import com.github.ykiselev.ag.grid.data.common.MapUtils;
import com.github.ykiselev.ag.grid.data.common.Predicates;
import com.github.ykiselev.ag.grid.data.types.TypeInfo;
import com.google.common.collect.Sets;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;
import static java.util.Objects.requireNonNull;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public final class ObjectSourceBasedAgGridRowSource<V> implements AgGridRowSource {

    private final ObjectSource<V> source;

    public ObjectSourceBasedAgGridRowSource(ObjectSource<V> source) {
        this.source = requireNonNull(source);
    }

    @Override
    public AgGridGetRowsResponse getRows(AgGridGetRowsRequest request) {
        final Context context = Context.create(request);
        final RequestFilters filters = DefaultRequestFilters.create(context.getRequest());
        try (FilteredObjectSource<V> filteredSource = source.filter(filters)) {
            final ResponseBuilder<V> builder = new ResponseBuilder<>(context, filteredSource);
            return builder.build();
        }
    }

    private static final class ResponseBuilder<V> {

        private final Context context;

        private final FilteredObjectSource<V> source;

        ResponseBuilder(Context context, FilteredObjectSource<V> source) {
            this.context = requireNonNull(context);
            this.source = requireNonNull(source);
        }

        AgGridGetRowsResponse build() {
            final Stream<V> rows = filter();
            final Stream<Map<String, Object>> result;
            if (context.isGrouping() || context.isPivot()) {
                result = Aggregation.groupBy(rows, context, source.getTypeInfo());
            } else {
/*                Stopwatch sw = Stopwatch.createStarted();
                Object[] array = rows.toArray();
                System.out.println("Array created in " + sw);
                sw.reset().start();
                context.getRequest()
                        .getSortModel()
                        .stream()
                        .map(this::comparator2)
                        .reduce(Comparator::thenComparing)
                        .ifPresent(cmp -> Arrays.parallelSort(array, (Comparator<Object>) cmp));
                System.out.println("Array sorted in " + sw);*/
                return context.createResponse(
                        limit2(sort2(rows))
                                .map(v -> source.getTypeInfo().toMap().apply((V) v))
                                .collect(Collectors.toList())
                );
                //result = convert(rows);
            }
            return context.createResponse(limit(sort(result)));
        }

        private Stream<Map<String, Object>> convert(Stream<V> rows) {
            return rows.map(source.getTypeInfo().toMap());
        }

        private Stream<V> filter() {
            // Source is free to ignore some or all original filters, so we need to filter by ignored filters here
            final RequestFilters filters = source.getFilters();
            final Set<String> toFilter =
                    Sets.difference(filters.getNames(), source.getFilteredNames());
            final Predicate<V> predicate = filter(toFilter, filters);
            return source.stream()
                    .filter(predicate);
        }

        private Predicate<V> filter(Set<String> columns, RequestFilters filters) {
            final Function<String, Predicate<V>> factory = col ->
                    Predicates.predicate(
                            source.getTypeInfo().getAttribute(col),
                            filters.getFilter(col)
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

        private Stream<V> sort2(Stream<V> src) {
            return context.getRequest()
                    .getSortModel()
                    .stream()
                    .map(this::comparator2)
                    .reduce(Comparator::thenComparing)
                    .map(src.parallel()::sorted)
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

        private Comparator<V> comparator2(SortModel sortModel) {
            final TypeInfo<V> typeInfo = source.getTypeInfo();
            Comparator<V> comparator = typeInfo.getAttribute(sortModel.getColId()).getComparator();
            if (Sorting.ASC != sortModel.getSort()) {
                comparator = comparator.reversed();
            }
            return nullsFirst(comparator);
        }

        private <T> Stream<T> limit2(Stream<T> src) {
            return src.skip(context.getRequest().getStartRow())
                    .limit(context.getRequest().getEndRow() + 1);
        }

        private <T> List<T> limit(Stream<T> src) {
            return src.skip(context.getRequest().getStartRow())
                    .limit(context.getRequest().getEndRow() + 1)
                    .collect(Collectors.toList());
        }
    }
}