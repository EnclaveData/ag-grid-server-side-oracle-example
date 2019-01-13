package com.ag.grid.enterprise.oracle.demo.dao;

import com.github.ykiselev.ag.grid.api.filter.ColumnFilter;
import com.github.ykiselev.ag.grid.api.filter.GroupKey;
import com.google.common.collect.ImmutableMap;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * @author Yuriy Kiselev (uze@yandex.ru)
 * @since 13.01.2019
 */
public final class MapBasedColumnIndex<K, C> implements ColumnIndex<K, C> {

    private final String column;

    private final Map<Object, List<K>> index;

    public MapBasedColumnIndex(String column, Map<Object, List<K>> index) {
        this.column = requireNonNull(column);
        this.index = ImmutableMap.copyOf(index);
    }

    @Override
    public String getColumn() {
        return column;
    }

    @Override
    public Collection<K> getKeys(C container, ColumnFilter filter) {
        final Predicate<Object> predicate = predicate(filter);
        return index.entrySet()
                .stream()
                .filter(e -> predicate.test(e.getKey()))
                .map(Map.Entry::getValue)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    private Predicate<Object> predicate(ColumnFilter filter) {
        if (filter instanceof GroupKey) {
            return k -> Objects.equals(k, ((GroupKey) filter).getFilter());
        }
        return k -> true;
    }

    @Override
    public String toString() {
        return "MapBasedColumnIndex{" +
                "column='" + column + '\'' +
                ", index=" + index.entrySet().stream().map(e -> e.getKey() + " : " + e.getValue().size()).collect(Collectors.joining(",")) +
                '}';
    }
}
