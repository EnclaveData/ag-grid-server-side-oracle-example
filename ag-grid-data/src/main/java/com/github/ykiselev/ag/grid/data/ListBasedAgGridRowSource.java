package com.github.ykiselev.ag.grid.data;

import com.github.ykiselev.ag.grid.api.request.AgGridGetRowsRequest;
import com.github.ykiselev.ag.grid.api.request.SortModel;
import com.github.ykiselev.ag.grid.api.request.Sorting;
import com.github.ykiselev.ag.grid.api.response.AgGridGetRowsResponse;
import com.github.ykiselev.ag.grid.data.aggregation.Aggregation;
import com.github.ykiselev.ag.grid.data.common.MapUtils;
import com.github.ykiselev.ag.grid.data.common.Predicates;
import com.github.ykiselev.ag.grid.data.types.TypeInfo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.BaseStream;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;
import static java.util.Objects.requireNonNull;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public final class ListBasedAgGridRowSource<V> implements AgGridRowSource {

    private final List<V> source;

    private final TypeInfo<V> typeInfo;

    public ListBasedAgGridRowSource(List<V> source, TypeInfo<V> typeInfo) {
        this.source = requireNonNull(source);
        this.typeInfo = requireNonNull(typeInfo);
    }

    @Override
    public AgGridGetRowsResponse getRows(AgGridGetRowsRequest request) {
        final Context context = Context.create(request);
        final RequestFilters filters = DefaultRequestFilters.create(context.getRequest());
        final Stream<V> src = source.stream()
                .filter(filter(filters.getNames(), filters));
        final Function<Stream<V>, List<Map<String, Object>>> builder;
        if (context.isGrouping() || context.isPivot()) {
            builder = new AggregatedResponseBuilder(context);
        } else {
            builder = new PlainResponseBuilder(context);
        }
        return context.createResponse(builder.apply(src.parallel()));
    }

    private Predicate<V> filter(Set<String> columns, RequestFilters filters) {
        final Function<String, Predicate<V>> factory = col ->
                Predicates.predicate(
                        typeInfo.getAttribute(col),
                        filters.getFilter(col)
                );
        return columns.stream()
                .map(factory)
                .reduce(Predicate::and)
                .orElse(v -> true);
    }

    private final class AggregatedResponseBuilder implements Function<Stream<V>, List<Map<String, Object>>> {

        private final Context context;

        AggregatedResponseBuilder(Context context) {
            this.context = requireNonNull(context);
        }

        @Override
        public List<Map<String, Object>> apply(Stream<V> rows) {
            return limit(sort(Aggregation.groupBy(rows, context, typeInfo)));
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

        private <T> List<T> limit(Stream<T> src) {
            return src.skip(context.getRequest().getStartRow())
                    .limit(context.getRequest().getEndRow() + 1)
                    .collect(Collectors.toList());
        }
    }

    private final class PlainResponseBuilder implements Function<Stream<V>, List<Map<String, Object>>> {

        private final Context context;

        PlainResponseBuilder(Context context) {
            this.context = requireNonNull(context);
        }

        @Override
        public List<Map<String, Object>> apply(Stream<V> rows) {
            return limit(sort(rows))
                    .map(typeInfo.toMap())
                    .collect(Collectors.toCollection(() -> new ArrayList<>(105)));
        }

        private Stream<V> sort(Stream<V> src) {
            return context.getRequest()
                    .getSortModel()
                    .stream()
                    .map(this::comparator)
                    .reduce(Comparator::thenComparing)
                    .map(src::sorted)
                    .orElse(src);
        }

        private Comparator<V> comparator(SortModel sortModel) {
            Comparator<V> comparator = typeInfo.getAttribute(sortModel.getColId()).getComparator();
            if (Sorting.ASC != sortModel.getSort()) {
                comparator = comparator.reversed();
            }
            return nullsFirst(comparator);
        }

        private <T> Stream<T> limit(Stream<T> src) {
            return src.skip(context.getRequest().getStartRow())
                    .limit(context.getRequest().getEndRow() + 1);
        }
    }
}