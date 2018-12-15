package com.ag.grid.enterprise.oracle.demo.builder;

import com.github.ykiselev.aggrid.domain.filter.NumberColumnFilter;
import com.tangosol.util.Filter;
import com.tangosol.util.ValueExtractor;
import com.tangosol.util.filter.AndFilter;
import com.tangosol.util.filter.EqualsFilter;
import com.tangosol.util.filter.GreaterEqualsFilter;
import com.tangosol.util.filter.GreaterFilter;
import com.tangosol.util.filter.LessEqualsFilter;
import com.tangosol.util.filter.LessFilter;
import com.tangosol.util.filter.NotEqualsFilter;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
@SuppressWarnings("unchecked")
final class CohFilters {

    static Filter between(NumberColumnFilter filter, ValueExtractor extractor) {
        return new AndFilter(
                new GreaterEqualsFilter(extractor, filter.getFilter()),
                new LessFilter(extractor, filter.getFilterTo())
        );
    }

    static Filter equals(NumberColumnFilter filter, ValueExtractor extractor) {
        return new EqualsFilter(extractor, filter.getFilter());
    }

    static Filter notEquals(NumberColumnFilter filter, ValueExtractor extractor) {
        return new NotEqualsFilter(extractor, filter.getFilter());
    }

    static Filter lessThan(NumberColumnFilter filter, ValueExtractor extractor) {
        return new LessFilter(extractor, filter.getFilter());
    }

    static Filter lessThanOrEqual(NumberColumnFilter filter, ValueExtractor extractor) {
        return new LessEqualsFilter(extractor, filter.getFilter());
    }

    static Filter greaterThan(NumberColumnFilter filter, ValueExtractor extractor) {
        return new GreaterFilter(extractor, filter.getFilter());
    }

    static Filter greaterThanOrEqual(NumberColumnFilter filter, ValueExtractor extractor) {
        return new GreaterEqualsFilter(extractor, filter.getFilter());
    }
}
