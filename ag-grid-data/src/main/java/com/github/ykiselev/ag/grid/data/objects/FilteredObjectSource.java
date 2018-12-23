package com.github.ykiselev.ag.grid.data.objects;

import com.github.ykiselev.ag.grid.data.RequestFilters;
import com.github.ykiselev.ag.grid.data.types.TypeInfo;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Filtered source. Instance of this class should be obtained via call to {@link ObjectSource#filter(RequestFilters)}.
 *
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public interface FilteredObjectSource<K, V> {

    /**
     * @return the columns for which the filter was already applied
     */
    Set<String> getFilteredNames();

    /**
     * Implementing class is expected to apply configured filter (at least the part that corresponds to key attributes).
     *
     * @return all the keys that passed the configured filter.
     */
    Iterable<K> getKeys();

    /**
     * Implementing class is expected to apply configured filter (the part that corresponds to item's attributes only) if keys was filtered using only key's attributes.
     *
     * @param keys the keys for which to return items. Should be some of the keys returned by {@link FilteredObjectSource#getKeys()}.
     * @return the list with found items (may be empty).
     */
    List<V> getAll(Collection<K> keys);

    /**
     * @return the type info for parameter {@code V}
     */
    TypeInfo<V> getTypeInfo();
}
