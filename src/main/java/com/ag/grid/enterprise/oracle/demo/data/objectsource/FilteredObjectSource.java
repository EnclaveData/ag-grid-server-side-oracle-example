package com.ag.grid.enterprise.oracle.demo.data.objectsource;

import java.util.Collection;
import java.util.List;

/**
 * Map source with configured filter. Instance of this class should be obtained by a call to {@link ObjectSource#filter(com.ag.grid.enterprise.oracle.demo.data.RequestFilters)}.
 *
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public interface FilteredObjectSource<K, V> {

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

}
