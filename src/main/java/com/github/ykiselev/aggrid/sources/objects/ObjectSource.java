package com.github.ykiselev.aggrid.sources.objects;

import com.github.ykiselev.aggrid.sources.RequestFilters;
import com.github.ykiselev.aggrid.sources.objects.types.TypeInfo;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public interface ObjectSource<K, V> {

    /**
     * Convenient method to create filtered map source. Implementation may parse supplied {@code filters} once and re-use
     * filters for consecutive calls to {@link FilteredObjectSource#getKeys()} and {@link FilteredObjectSource#getAll(java.util.Collection)}.
     * Note that implementation is free to apply supplied filters to keys only or not to apply filters at all. In such
     * case it is important to return correct set of columns by which filtering was applied via {@link FilteredObjectSource#getFilteredNames()}
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
