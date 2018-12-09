package com.ag.grid.enterprise.oracle.demo.data.mapsource;

import com.ag.grid.enterprise.oracle.demo.data.RequestFilters;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public interface MapSource<K> {

    /**
     * Convenient method to create filtered map source. Implementation may parse supplied {@code filters} once and re-use
     * filters for consecutive calls to {@link FilteredMapSource#getKeys()} and {@link com.ag.grid.enterprise.oracle.demo.data.mapsource.FilteredMapSource#getAll(java.util.Collection)}.
     *
     * @param filters the filters to apply.
     * @return map source that applies configured filter when returning keys and values.
     */
    FilteredMapSource<K> filter(RequestFilters filters);

}
