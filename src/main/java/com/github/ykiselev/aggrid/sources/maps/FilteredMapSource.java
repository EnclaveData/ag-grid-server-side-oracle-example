package com.github.ykiselev.aggrid.sources.maps;

import com.github.ykiselev.aggrid.sources.RequestFilters;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Map source with configured filter. Instance of this class should be obtained by a call to {@link MapSource#filter(RequestFilters)}.
 *
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public interface FilteredMapSource<K> {

    /**
     * Implementing class is expected to apply configured filter (at least the part that corresponds to key attributes).
     *
     * @return all the keys that passed the configured filter.
     */
    Iterable<K> getKeys();

    /**
     * Implementing class is expected to apply configured filter (the part that corresponds to item's attributes only) if keys was filtered using only key's attributes.
     *
     * @param keys the keys for which to return items. Should be some of the keys returned by {@link FilteredMapSource#getKeys()}.
     * @return the list with found items (may be empty).
     */
    List<Map<String, Object>> getAll(Collection<K> keys);

}
