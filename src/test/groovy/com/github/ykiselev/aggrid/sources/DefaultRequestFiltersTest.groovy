package com.github.ykiselev.aggrid.sources

import com.github.ykiselev.aggrid.domain.filter.GroupKey
import com.github.ykiselev.aggrid.domain.filter.TextColumnFilter
import com.github.ykiselev.aggrid.domain.request.AgGridGetRowsRequest
import com.github.ykiselev.aggrid.domain.request.ColumnVO
import spock.lang.Specification

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
class DefaultRequestFiltersTest extends Specification {

    def "should fill in filters"() {
        given:
        def cf1 = new TextColumnFilter('startsWith', 'xyz')
        def cf2 = new TextColumnFilter('endsWith', 'abc')
        def req = new AgGridGetRowsRequest()
        req.setFilterModel([
                ('a'): cf1,
                ('b'): cf2
        ] as Map)
        req.setGroupKeys(['value1'])
        req.setRowGroupCols([new ColumnVO('VALUE', 'Value', 'c', null)])

        when:
        def f = DefaultRequestFilters.create(req)

        then:
        f.getNames() as Set == ['a', 'b', 'c'] as Set
        f.getColumnFilter('a') == cf1
        f.getColumnFilter('b') == cf2
        def f3 = f.getColumnFilter('c')
        (f3 as GroupKey).filter == 'value1'
    }
}
