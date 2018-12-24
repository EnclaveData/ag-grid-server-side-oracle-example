package com.github.ykiselev.ag.grid.data;

import com.github.ykiselev.ag.grid.data.types.TypeInfo;

import java.util.Set;
import java.util.stream.Stream;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public interface ObjectSource<V> {

    /**
     * @return the columns for which the filter was already applied
     */
    Set<String> getFilteredNames();

    /**
     * Implementing class is expected to apply supplied filter (if possible).
     *
     * @return all the keys that passed the configured filter.
     */
    Stream<V> getAll(RequestFilters filters);

    /**
     * @return the type info for parameter {@code V}
     */
    TypeInfo<V> getTypeInfo();

}
