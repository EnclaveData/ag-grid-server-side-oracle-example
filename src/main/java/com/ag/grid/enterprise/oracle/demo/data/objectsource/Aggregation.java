package com.ag.grid.enterprise.oracle.demo.data.objectsource;

import com.ag.grid.enterprise.oracle.demo.data.Context;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
final class Aggregation {

    static <K, V> Collector<V, ?, Map> create(Context context, ObjectSource<K, V> source) {
        final List<Function<V, Object>> classifiers;
        if (context.isPivot()) {
            classifiers =
                    Stream.concat(
                            context.getGroupByColumns().stream(),
                            context.getPivotColumns().stream()
                    ).map(source::createExtractor)
                            .collect(Collectors.toList());
        } else {
            classifiers =
                    context.getGroupByColumns()
                            .stream()
                            .map(source::createExtractor)
                            .collect(Collectors.toList());
        }

        // We need to compose collectors from last to first, so that outer one would be the first grouping column
        final List<Function<V, Object>> reversed = Lists.reverse(classifiers);

        Collector grouping = Collectors.groupingBy(
                reversed.get(0),
                Collectors.collectingAndThen(
                        Collectors.reducing(
                                source.createMerge(context.getColumnsToMerge())
                        ),
                        o -> o.orElse(null)
                )
        );
        for (int i = 1; i < reversed.size(); i++) {
            grouping = Collectors.groupingBy(reversed.get(i), grouping);
        }
        return grouping;
    }
}