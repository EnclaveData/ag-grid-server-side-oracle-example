package com.ag.grid.enterprise.oracle.demo.data.mapsource;

import com.ag.grid.enterprise.oracle.demo.data.Context;
import com.ag.grid.enterprise.oracle.demo.request.ColumnVO;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
final class Aggregation {

    static Stream<Map<String, Object>> transform(Stream<Map<String, Object>> src, Context context) {
        return new AggregationResult(context, src.collect(create(context))).parse();
    }

    static Collector<Map<String, Object>, ?, Map<String, Object>> create(Context context) {
        final List<Function<Map<String, Object>, String>> classifiers;
        if (context.isPivot()) {
            classifiers =
                    Stream.concat(
                            context.getGroupByColumns().stream(),
                            context.getPivotColumns().stream()
                    ).map(MapUtils::extractValue)
                            .collect(Collectors.toList());
        } else {
            classifiers =
                    context.getGroupByColumns()
                            .stream()
                            .map(MapUtils::extractValue)
                            .collect(Collectors.toList());
        }

        // We need to compose collectors from last to first, so that outer one would be the first grouping column
        final List<Function<Map<String, Object>, String>> reversed = Lists.reverse(classifiers);

        Collector grouping = Collectors.groupingBy(
                reversed.get(0),
                Collectors.reducing(
                        new HashMap<>(),
                        MergeOperator.create(context)
                )
        );
        for (int i = 1; i < reversed.size(); i++) {
            grouping = Collectors.groupingBy(reversed.get(i), grouping);
        }
        return grouping;
    }
}

/**
 * todo - we need real merger!
 */
final class MergeOperator implements BinaryOperator<Map<String, Object>> {

    private final Object UNDEFINED = new Object();

    private final Context context;

    private final Set<String> groupByColumns;

    private final Set<String> valueColumns;

    private MergeOperator(Context context, Set<String> groupByColumns, Set<String> valueColumns) {
        this.context = requireNonNull(context);
        this.groupByColumns = requireNonNull(groupByColumns);
        this.valueColumns = requireNonNull(valueColumns);
    }

    @Override
    public Map<String, Object> apply(Map<String, Object> a, Map<String, Object> b) {
        return Sets.union(a.keySet(), b.keySet())
                .stream()
                .filter(col -> groupByColumns.contains(col) || valueColumns.contains(col))
                .collect(Collectors.toMap(
                        k -> k,
                        k -> mergeValues(k, a.getOrDefault(k, UNDEFINED), b.getOrDefault(k, UNDEFINED))
                ));
    }

    private Object mergeValues(String name, Object a, Object b) {
        if (name == null) {
            int g = 0;
        }
        if (Objects.equals(a, b)) {
            return a;
        }
        if (a == UNDEFINED) {
            return b;
        }
        if (b == UNDEFINED) {
            return a;
        }
        if (groupByColumns.contains(name)) {
            if (!Objects.equals(a, b)) {
                throw new IllegalStateException("Different values in group by column: " + name + ", " + a + " <> " + b);
            }
            return a;
        }
        final ColumnVO column = context.getColumn(name);
        final String func = column.getAggFunc();
        if (func == null) {
            throw new IllegalStateException("Unable to merge - no aggregation function: " + name);
        }
        switch (func) {
            case "sum":
                return sum(a, b);

            default:
                throw new UnsupportedOperationException("" + func);
        }
    }

    private Object sum(Object a, Object b) {
        if (a instanceof Double) {
            return (double) a + (double) b;
        }
        if (a instanceof Integer) {
            return (int) a + (int) b;
        }
        if (a instanceof Long) {
            return (long) a + (long) b;
        }
        if (a instanceof Float) {
            return (float) a + (float) b;
        }
        if (a instanceof Short) {
            return (short) a + (short) b;
        }
        if (a instanceof Byte) {
            return (byte) a + (byte) b;
        }
        throw new IllegalArgumentException("Unable to sum " + a + " and " + b);
    }

    static MergeOperator create(Context context) {
        return new MergeOperator(
                context,
                ImmutableSet.copyOf(context.getGroupByColumns()),
                ImmutableSet.copyOf(context.getValueColumns())
        );
    }
}