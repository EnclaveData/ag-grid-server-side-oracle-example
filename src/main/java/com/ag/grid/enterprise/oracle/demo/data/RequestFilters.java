package com.ag.grid.enterprise.oracle.demo.data;

import com.ag.grid.enterprise.oracle.demo.filter.ColumnFilter;
import com.ag.grid.enterprise.oracle.demo.request.ColumnVO;

import java.util.Set;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public interface RequestFilters {

    /**
     * @see ColumnVO#getField()
     */
    Set<String> getNames();

    /**
     * @see ColumnVO#getField()
     */
    ColumnFilter getColumnFilter(String name);
}
