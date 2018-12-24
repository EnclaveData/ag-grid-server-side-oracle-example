package com.ag.grid.enterprise.oracle.demo.builder;

import com.github.ykiselev.ag.grid.api.filter.ColumnFilter;
import com.github.ykiselev.ag.grid.api.filter.GroupKey;
import com.github.ykiselev.ag.grid.api.filter.NumberColumnFilter;
import com.github.ykiselev.ag.grid.api.filter.SetColumnFilter;
import com.github.ykiselev.ag.grid.api.filter.TextColumnFilter;
import com.github.ykiselev.ag.grid.data.RequestFilters;
import com.google.common.collect.ImmutableMap;
import com.tangosol.util.Filter;
import com.tangosol.util.ValueExtractor;
import com.tangosol.util.extractor.ReflectionExtractor;
import com.tangosol.util.filter.AllFilter;
import com.tangosol.util.filter.AlwaysFilter;
import com.tangosol.util.filter.AndFilter;
import com.tangosol.util.filter.EqualsFilter;
import com.tangosol.util.filter.GreaterEqualsFilter;
import com.tangosol.util.filter.GreaterFilter;
import com.tangosol.util.filter.InFilter;
import com.tangosol.util.filter.LessEqualsFilter;
import com.tangosol.util.filter.LessFilter;
import com.tangosol.util.filter.LikeFilter;
import com.tangosol.util.filter.NotEqualsFilter;
import com.tangosol.util.filter.NotFilter;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Stream;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
@SuppressWarnings("unchecked")
public final class CohFilters {

    private static final Map<String, BiFunction<NumberColumnFilter, ValueExtractor, Filter>> numberFilterMap =
            ImmutableMap.<String, BiFunction<NumberColumnFilter, ValueExtractor, Filter>>builder()
                    .put("inRange", CohFilters::between)
                    .put("equals", CohFilters::equals)
                    .put("notEqual", CohFilters::notEquals)
                    .put("lessThan", CohFilters::lessThan)
                    .put("lessThanOrEqual", CohFilters::lessThanOrEqual)
                    .put("greaterThan", CohFilters::greaterThan)
                    .put("greaterThanOrEqual", CohFilters::greaterThanOrEqual)
                    .build();

    private static Filter between(NumberColumnFilter filter, ValueExtractor extractor) {
        return new AndFilter(
                new GreaterEqualsFilter(extractor, filter.getFilter()),
                new LessFilter(extractor, filter.getFilterTo())
        );
    }

    private static Filter equals(NumberColumnFilter filter, ValueExtractor extractor) {
        return new EqualsFilter(extractor, filter.getFilter());
    }

    private static Filter notEquals(NumberColumnFilter filter, ValueExtractor extractor) {
        return new NotEqualsFilter(extractor, filter.getFilter());
    }

    private static Filter lessThan(NumberColumnFilter filter, ValueExtractor extractor) {
        return new LessFilter(extractor, filter.getFilter());
    }

    private static Filter lessThanOrEqual(NumberColumnFilter filter, ValueExtractor extractor) {
        return new LessEqualsFilter(extractor, filter.getFilter());
    }

    private static Filter greaterThan(NumberColumnFilter filter, ValueExtractor extractor) {
        return new GreaterFilter(extractor, filter.getFilter());
    }

    private static Filter greaterThanOrEqual(NumberColumnFilter filter, ValueExtractor extractor) {
        return new GreaterEqualsFilter(extractor, filter.getFilter());
    }

    // todo - limited draft code!
    public static String getterFor(String field) {
        return "get" + Character.toUpperCase(field.charAt(0)) + field.substring(1);
    }

    private static Filter setFilter(ValueExtractor extractor, SetColumnFilter filter) {
        return new InFilter<>(extractor, filter.getValues());
    }

    private static Filter numberFilter(ValueExtractor extractor, NumberColumnFilter filter) {
        switch (filter.getType()) {
            case EQUALS:
                return equals(filter, extractor);

            case NOT_EQUAL:
                return notEquals(filter, extractor);

            case LESS_THAN:
                return lessThan(filter, extractor);

            case LESS_THAN_OR_EQUAL:
                return lessThanOrEqual(filter, extractor);

            case GREATER_THAN:
                return greaterThan(filter, extractor);

            case GREATER_THAN_OR_EQUAL:
                return greaterThanOrEqual(filter, extractor);

            case IN_RANGE:
                return between(filter, extractor);

            default:
                throw new IllegalArgumentException("Unknown filter type: " + filter.getType());
        }
    }

    private static Filter textFilter(ValueExtractor extractor, TextColumnFilter filter) {
        switch (filter.getType()) {
            case EQUALS:
                return new EqualsFilter(extractor, filter.getFilter());

            case NOT_EQUAL:
                return new NotEqualsFilter(extractor, filter.getFilter());

            case CONTAINS:
                return new LikeFilter(extractor, "%" + filter.getFilter() + "%");

            case NOT_CONTAINS:
                return new NotFilter(new LikeFilter(extractor, "%" + filter.getFilter() + "%"));

            case STARTS_WITH:
                return new LikeFilter(extractor, filter.getFilter() + "%");

            case ENDS_WITH:
                return new LikeFilter(extractor, "%" + filter.getFilter());

            default:
                throw new IllegalArgumentException("Unknown filter type: " + filter.getType());
        }
    }

    private static Filter groupKey(ValueExtractor extractor, GroupKey groupKey) {
        // todo - what if value type isn't string?
        return new EqualsFilter(extractor, groupKey.getFilter());
    }

    public static Filter toFilter(ColumnFilter filter, ValueExtractor extractor) {
        if (filter instanceof SetColumnFilter) {
            return setFilter(extractor, (SetColumnFilter) filter);
        }
        if (filter instanceof NumberColumnFilter) {
            return numberFilter(extractor, (NumberColumnFilter) filter);
        }
        if (filter instanceof TextColumnFilter) {
            return textFilter(extractor, (TextColumnFilter) filter);
        }
        if (filter instanceof GroupKey) {
            return groupKey(extractor, (GroupKey) filter);
        }
        return AlwaysFilter.INSTANCE;
    }

    public static Stream<Filter> getFilters(Map<String, ColumnFilter> filterMap) {
        return filterMap.entrySet()
                .stream()
                .map(e -> toFilter(e.getValue(), new ReflectionExtractor(getterFor(e.getKey()))));
    }

    public static Filter filter(RequestFilters filters) {
        final Filter[] array = filters.getNames()
                .stream()
                .map(col -> toFilter(filters.getColumnFilter(col), new ReflectionExtractor(getterFor(col))))
                .toArray(Filter[]::new);
        if (array.length > 0) {
            return new AllFilter(array);
        }
        return AlwaysFilter.INSTANCE;
    }
}
