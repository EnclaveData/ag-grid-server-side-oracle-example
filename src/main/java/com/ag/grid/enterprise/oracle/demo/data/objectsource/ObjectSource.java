package com.ag.grid.enterprise.oracle.demo.data.objectsource;

import com.ag.grid.enterprise.oracle.demo.data.RequestFilters;
import com.ag.grid.enterprise.oracle.demo.data.types.TypeInfo;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public interface ObjectSource<K, V> {

    /**
     * Convenient method to create filtered map source. Implementation may parse supplied {@code filters} once and re-use
     * filters for consecutive calls to {@link FilteredObjectSource#getKeys()} and {@link FilteredObjectSource#getAll(java.util.Collection)}.
     *
     * @param filters the filters to apply.
     * @return map source that applies configured filter when returning keys and values.
     */
    FilteredObjectSource<K, V> filter(RequestFilters filters);

    /**
     * @return the type info for parameter {@code V}
     */
    TypeInfo<V> getTypeInfo();
}
