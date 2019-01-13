package com.ag.grid.enterprise.oracle.demo.dao;

import com.github.ykiselev.ag.grid.api.filter.ColumnFilter;

import java.util.Collection;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * @author Yuriy Kiselev (uze@yandex.ru)
 * @since 13.01.2019
 */
public final class NoopColumnIndex<K, C> implements ColumnIndex<K, C> {

    private final String column;

    private final Function<C, Collection<K>> toKeys;

    public NoopColumnIndex(String column, Function<C, Collection<K>> toKeys) {
        this.column = requireNonNull(column);
        this.toKeys = requireNonNull(toKeys);
    }

    @Override
    public String getColumn() {
        return column;
    }

    @Override
    public Collection<K> getKeys(C container, ColumnFilter filter) {
        return toKeys.apply(container);
    }
}
