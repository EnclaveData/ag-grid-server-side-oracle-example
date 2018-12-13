package com.ag.grid.enterprise.oracle.demo.data.objectsource;

import com.ag.grid.enterprise.oracle.demo.data.types.TypeInfo;
import com.ag.grid.enterprise.oracle.demo.filter.ColumnFilter;
import com.ag.grid.enterprise.oracle.demo.filter.GroupKey;
import com.ag.grid.enterprise.oracle.demo.filter.NumberColumnFilter;
import com.ag.grid.enterprise.oracle.demo.filter.SetColumnFilter;
import com.ag.grid.enterprise.oracle.demo.filter.TextColumnFilter;
import com.google.common.collect.ImmutableMap;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.IntPredicate;
import java.util.function.Predicate;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
@Deprecated
public final class ColumnFilters {

    private static final Map<String, BiFunction<String, NumberColumnFilter, Predicate<Map<String, Object>>>> numberPredicateFactories =
            ImmutableMap.<String, BiFunction<String, NumberColumnFilter, Predicate<Map<String, Object>>>>builder()
                    .put("equals", ColumnFilters::equals2)
                    .put("notEqual", ColumnFilters::notEqual)
                    .put("lessThanOrEqual", ColumnFilters::lessThanOrEqual)
                    .put("greaterThan", ColumnFilters::greaterThan)
                    .put("greaterThanOrEqual", ColumnFilters::greaterThanOrEqual)
                    .put("inRange", ColumnFilters::between)
                    .build();

    private static final Map<String, BiFunction<String, TextColumnFilter, Predicate<Map<String, Object>>>> textPredicateFactories =
            ImmutableMap.<String, BiFunction<String, TextColumnFilter, Predicate<Map<String, Object>>>>builder()
                    .put("equals", ColumnFilters::equals2)
                    .put("notEqual", ColumnFilters::notEqual)
                    .put("contains", ColumnFilters::contains)
                    .put("notContains", ColumnFilters::notContains)
                    .put("startsWith", ColumnFilters::startsWith)
                    .put("endsWith", ColumnFilters::endsWith)
                    .build();

    public static Predicate<Map<String, Object>> predicate(String name, ColumnFilter filter, TypeInfo info) {
        final Predicate<Map<String, Object>> result;
        if (filter instanceof NumberColumnFilter) {
            result = predicate(name, ((NumberColumnFilter) filter));
        } else if (filter instanceof SetColumnFilter) {
            result = predicate(name, (SetColumnFilter) filter);
        } else if (filter instanceof TextColumnFilter) {
            result = predicate(name, (TextColumnFilter) filter);
        } else if (filter instanceof GroupKey) {
            result = predicate(name, (GroupKey) filter);
        } else {
            throw new IllegalArgumentException("Unsupported filter: " + filter);
        }
        return result;
    }

    public static Predicate<Map<String, Object>> predicate(String name, NumberColumnFilter filter) {
        return numberPredicateFactories.get(filter.getFilterType()).apply(name, filter);
    }

    public static Predicate<Map<String, Object>> predicate(String name, TextColumnFilter filter) {
        return textPredicateFactories.get(filter.getFilterType()).apply(name, filter);
    }

    public static Predicate<Map<String, Object>> predicate(String name, SetColumnFilter filter) {
        final Set<String> set = new HashSet<>(filter.getValues());
        return m -> set.contains(m.get(name));
    }

    public static Predicate<Map<String, Object>> predicate(String name, GroupKey filter) {
        return m -> Objects.equals(m.get(name), filter.getFilter());
    }

    private static Predicate<Map<String, Object>> equals2(String column, NumberColumnFilter filter) {
        return m -> Objects.equals(m.get(column), filter.getFilter());
    }

    private static Predicate<Map<String, Object>> equals2(String column, TextColumnFilter filter) {
        return m -> Objects.equals(m.get(column), filter.getFilter());
    }

    private static Predicate<Map<String, Object>> notEqual(String column, NumberColumnFilter filter) {
        return equals2(column, filter).negate();
    }

    private static Predicate<Map<String, Object>> notEqual(String column, TextColumnFilter filter) {
        return equals2(column, filter).negate();
    }

    private static boolean compare(Object a, Object b, IntPredicate expected) {
        return a instanceof Comparable && expected.test(((Comparable) a).compareTo(b));
    }

    private static Predicate<Map<String, Object>> lessThanOrEqual(String column, NumberColumnFilter filter) {
        return m -> compare(m.get(column), filter.getFilter(), r -> r <= 0);
    }

    private static Predicate<Map<String, Object>> greaterThan(String column, NumberColumnFilter filter) {
        return m -> compare(m.get(column), filter.getFilter(), r -> r > 0);
    }

    private static Predicate<Map<String, Object>> greaterThanOrEqual(String column, NumberColumnFilter filter) {
        return m -> compare(m.get(column), filter.getFilter(), r -> r >= 0);
    }

    private static Predicate<Map<String, Object>> between(String column, NumberColumnFilter filter) {
        return m -> compare(m.get(column), filter.getFilter(), r -> r >= 0) && compare(m.get(column), filter.getFilter(), r -> r < filter.getFilterTo());
    }

    private static Predicate<Map<String, Object>> str(String column, Predicate<String> p) {
        return m -> {
            final Object v = m.get(column);
            return v instanceof String && p.test((String) v);
        };
    }

    private static Predicate<Map<String, Object>> contains(String column, TextColumnFilter filter) {
        return str(column, s -> s.contains(filter.getFilter()));
    }

    private static Predicate<Map<String, Object>> notContains(String column, TextColumnFilter filter) {
        return contains(column, filter).negate();
    }

    private static Predicate<Map<String, Object>> startsWith(String column, TextColumnFilter filter) {
        return str(column, s -> s.startsWith(filter.getFilter()));
    }

    private static Predicate<Map<String, Object>> endsWith(String column, TextColumnFilter filter) {
        return str(column, s -> s.endsWith(filter.getFilter()));
    }
}
