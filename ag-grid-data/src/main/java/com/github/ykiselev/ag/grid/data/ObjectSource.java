package com.github.ykiselev.ag.grid.data;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public interface ObjectSource<V> {

    /**
     * @param filters the filters to apply
     * @return the optionally filtered object source
     */
    FilteredObjectSource<V> filter(RequestFilters filters);
}
