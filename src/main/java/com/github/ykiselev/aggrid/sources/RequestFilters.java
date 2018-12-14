package com.github.ykiselev.aggrid.sources;

import com.github.ykiselev.aggrid.domain.filter.ColumnFilter;
import com.github.ykiselev.aggrid.domain.request.ColumnVO;

import java.util.Set;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public interface RequestFilters {

    /**
     * @return set of column names for which {@link ColumnFilter} is defined
     * @see ColumnVO#getField()
     */
    Set<String> getNames();

    /**
     * @return column filter for specified name
     * @see ColumnVO#getField()
     */
    ColumnFilter getColumnFilter(String name);
}
