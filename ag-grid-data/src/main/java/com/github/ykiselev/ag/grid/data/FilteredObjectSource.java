package com.github.ykiselev.ag.grid.data;

import com.github.ykiselev.ag.grid.data.types.TypeInfo;

import java.util.Set;
import java.util.stream.Stream;

/**
 * @author Yuriy Kiselev (uze@yandex.ru)
 * @since 13.01.2019
 */
public interface FilteredObjectSource<V> extends AutoCloseable {

    /**
     * @return the type info for parameter {@code V}
     */
    TypeInfo<V> getTypeInfo();

    /**
     * @return request filters used to create this instance.
     */
    RequestFilters getFilters();

    /**
     * @return the columns for which the filter was already applied
     */
    Set<String> getFilteredNames();

    /**
     * @return optionally filtered stream.
     */
    Stream<V> stream();

    @Override
    void close();
}
