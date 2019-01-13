package com.ag.grid.enterprise.oracle.demo.dao;

import com.github.ykiselev.ag.grid.api.filter.ColumnFilter;

import java.util.Collection;

/**
 * @author Yuriy Kiselev (uze@yandex.ru)
 * @since 13.01.2019
 */
public interface ColumnIndex<K, C> {

    String getColumn();

    Collection<K> getKeys(C container, ColumnFilter filter);
}
