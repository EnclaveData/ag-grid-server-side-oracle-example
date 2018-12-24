package com.github.ykiselev.ag.grid.data.sources

import com.github.ykiselev.ag.grid.api.filter.GroupKey
import com.github.ykiselev.ag.grid.api.filter.TextColumnFilter
import com.github.ykiselev.ag.grid.api.filter.TextFilterType
import com.github.ykiselev.ag.grid.api.request.AgGridGetRowsRequest
import com.github.ykiselev.ag.grid.api.request.ColumnVO
import com.github.ykiselev.ag.grid.data.DefaultRequestFilters
import spock.lang.Specification

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
class DefaultRequestFiltersTest extends Specification {

    def "should fill in filters"() {
        given:
        def cf1 = new TextColumnFilter(TextFilterType.STARTS_WITH, 'xyz')
        def cf2 = new TextColumnFilter(TextFilterType.ENDS_WITH, 'abc')
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
