package com.github.ykiselev.aggrid.sources.objects.aggregation;

import com.github.ykiselev.aggrid.sources.Context;
import com.github.ykiselev.aggrid.sources.objects.ObjectSource;
import com.github.ykiselev.aggrid.sources.objects.types.Attribute;
import com.google.common.collect.Lists;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public final class Aggregation {

    private static <K, V> Collector<V, ?, Map> create(Context context, ObjectSource<K, V> source) {
        final Map<String, Attribute<V>> attrs = source.getTypeInfo().getAttributes();
        final Function<String, Function<V, ?>> getterFactory =
                col -> attrs.get(col).getObjectGetter();

        final Stream<String> classifierColumns;
        if (context.isPivot()) {
            classifierColumns =
                    Stream.concat(
                            context.getGroupByColumns().stream(),
                            context.getPivotColumns().stream()
                    );
        } else {
            classifierColumns =
                    context.getGroupByColumns()
                            .stream();
        }
        // We need to compose collectors from last to first, so that outer one would be the first grouping column
        final List<Function<V, ?>> classifiers = Lists.reverse(
                classifierColumns.map(getterFactory)
                        .collect(Collectors.toList())
        );

        Collector grouping = Collectors.groupingBy(classifiers.get(0), Aggregators.createCollector(context, source));

        for (int i = 1; i < classifiers.size(); i++) {
            grouping = Collectors.groupingBy(classifiers.get(i), grouping);
        }
        return grouping;
    }

    public static <K, V> Stream<Map<String, Object>> groupBy(Stream<V> input, Context context, ObjectSource<K, V> source) {
        if (context.isGrouping() || context.isPivot()) {
            return new AggregationResult(context, input.collect(create(context, source))).parse();
        }
        return input.map(source.getTypeInfo().toMap());
    }

    private static final class AggregationResult {

        private final Context context;

        private final Map<?, ?> rawResult;

        private final int maxDepth;

        AggregationResult(Context context, Map<?, ?> rawResult) {
            this.context = requireNonNull(context);
            this.rawResult = requireNonNull(rawResult);
            this.maxDepth = context.getGroupByColumns().size() + context.getPivotColumns().size();
        }

        Stream<Map<String, Object>> parse() {
            if (rawResult == null) {
                throw new NullPointerException("result");
            }
            return expandGroups(rawResult)
                    .map(this::addSecondaryColumns);
        }

        private Stream<Map<?, ?>> expandGroups(Map<?, ?> map) {
            return expandGroup(0, map);
        }

        private Stream<Map<?, ?>> expandGroup(int index, Map<?, ?> map) {
            final List<String> groupByColumns = context.getGroupByColumns();
            if (index < groupByColumns.size()) {
                return map.entrySet()
                        .stream()
                        .flatMap(e -> expandGroup(index + 1,
                                //new Node(parent, groupByColumns.get(index), e.getKey()),
                                (Map<?, ?>) e.getValue()));
            }
            return Stream.of(map);
        }

        private Map<String, Object> addSecondaryColumns(Map<?, ?> map) {
            // todo
            return (Map<String, Object>) map;
            //throw new UnsupportedOperationException("not implemented");
        }

        private String getColumnName(int index) {
            final List<String> keys = context.getGroupByColumns();
            if (index < keys.size()) {
                return keys.get(index);
            }
            return context.getPivotColumns().get(index - keys.size());
        }

        private Stream<Map<String, Object>> toRows(Map.Entry<?, ?> row) {
            final Map<String, Object> result = new HashMap<>();
            append(0, new Node(null, getColumnName(0), row.getKey(), null), row.getValue(), result);
            // todo flatten groups, pivot secondary columns, etc!
            return Stream.of(result);
        }

        private static String name(String parentName, String name) {
            if (parentName != null) {
                return parentName + "_" + name;
            }
            return name;
        }

        private void append(int index, Node node, Object value, Map<String, Object> target) {
            if (value instanceof Map) {
                final int idx = index + 1;
                if (idx < maxDepth) {
                    final String name = getColumnName(idx);
                    final Function<Object, String> path;
                    if (idx <= context.getGroupKeyCount()) {
                        path = Objects::toString;
                    } else {
                        path = k -> name(node.getPath(), Objects.toString(k));
                    }
                    ((Map<?, ?>) value).forEach((k, v) ->
                            append(idx, new Node(node, name, k, path.apply(k)), v, target));
                } else {
                    append(node, value, target);
                }
            } else {
                append(node, value, target);
            }
        }

        private void append(Node parent, Object value, Map<String, Object> target) {
            Node n = parent;
            while (n != null) {
                target.put(n.getName(), n.getKey());
                n = n.getParent();
            }
            final Map<String, String> column2secondary = context.getValueColumns()
                    .stream()
                    .collect(Collectors.toMap(
                            col -> col,
                            col -> name(parent.getPath(), col)
                    ));
            context.addSecondaryColumns(column2secondary.values());
            if (value instanceof Map) {
                ((Map<String, Object>) value).entrySet()
                        .stream()
                        .filter(e -> column2secondary.containsKey(e.getKey()))
                        .forEach(e ->
                                target.put(
                                        column2secondary.get(e.getKey()),
                                        e.getValue()
                                )
                        );
            } else {
                throw new IllegalStateException("Unsupported type: " + value);
            }
        }
    }

    private static final class Node {

        private Node parent;

        private final String path;

        private final String name;

        private final Object key;

        String getPath() {
            return path;
        }

        Node getParent() {
            return parent;
        }

        String getName() {
            return name;
        }

        Object getKey() {
            return key;
        }

        Node(Node parent, String name, Object key, String path) {
            this.parent = parent;
            this.name = name;
            this.key = key;
            this.path = path;
        }

        Node(Node parent, String name, Object key) {
            this(parent, name, key, null);
        }
    }
}