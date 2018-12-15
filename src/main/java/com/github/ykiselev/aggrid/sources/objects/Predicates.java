package com.github.ykiselev.aggrid.sources.objects;

import com.github.ykiselev.aggrid.domain.filter.ColumnFilter;
import com.github.ykiselev.aggrid.domain.filter.GroupKey;
import com.github.ykiselev.aggrid.domain.filter.NumberColumnFilter;
import com.github.ykiselev.aggrid.domain.filter.SetColumnFilter;
import com.github.ykiselev.aggrid.domain.filter.TextColumnFilter;
import com.github.ykiselev.aggrid.sources.objects.types.Attribute;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public final class Predicates {

    @SuppressWarnings("unchecked")
    public static <V> Predicate<V> predicate(Attribute<V> attr, ColumnFilter filter) {
        final Predicate<V> result;
        if (filter instanceof NumberColumnFilter) {
            result = predicate(attr.getIntGetter(), ((NumberColumnFilter) filter));
        } else if (filter instanceof SetColumnFilter) {
            result = predicate((Function<V, String>) attr.getObjectGetter(), (SetColumnFilter) filter);
        } else if (filter instanceof TextColumnFilter) {
            result = predicate((Function<V, String>) attr.getObjectGetter(), (TextColumnFilter) filter);
        } else if (filter instanceof GroupKey) {
            result = predicate((Function<V, String>) attr.getObjectGetter(), (GroupKey) filter);
        } else {
            throw new IllegalArgumentException("Unsupported filter: " + filter);
        }
        return result;
    }

    public static <V> Predicate<V> predicate(ToIntFunction<V> getter, NumberColumnFilter filter) {
        switch (filter.getType()) {
            case "equals":
                return equals2(getter, filter);

            case "notEqual":
                return equals2(getter, filter).negate();

            case "lessThan":
                return lessThan(getter, filter);

            case "lessThanOrEqual":
                return lessThanOrEqual(getter, filter);

            case "greaterThan":
                return greaterThan(getter, filter);

            case "greaterThanOrEqual":
                return greaterThanOrEqual(getter, filter);

            case "inRange":
                return between(getter, filter);

            default:
                throw new IllegalArgumentException("Unknown filter type: " + filter.getType());
        }
    }

    public static <V> Predicate<V> predicate(Function<V, String> getter, TextColumnFilter filter) {
        switch (filter.getType()) {
            case "equals":
                return equals2(getter, filter);

            case "notEqual":
                return equals2(getter, filter).negate();

            case "contains":
                return contains(getter, filter);

            case "notContains":
                return notContains(getter, filter);

            case "startsWith":
                return startsWith(getter, filter);

            case "endsWith":
                return endsWith(getter, filter);

            default:
                throw new IllegalArgumentException("Unknown filter type: " + filter.getType());
        }
    }

    public static <V> Predicate<V> predicate(Function<V, String> getter, SetColumnFilter filter) {
        return v -> filter.getValues().contains(getter.apply(v));
    }

    public static <V> Predicate<V> predicate(Function<V, String> getter, GroupKey filter) {
        return v -> Objects.equals(getter.apply(v), filter.getFilter());
    }

    private static <V> Predicate<V> equals2(ToIntFunction<V> getter, NumberColumnFilter filter) {
        return v -> Objects.equals(getter.applyAsInt(v), filter.getFilter());
    }

    private static <V> Predicate<V> equals2(Function<V, String> getter, TextColumnFilter filter) {
        return v -> Objects.equals(getter.apply(v), filter.getFilter());
    }

    @SuppressWarnings("unchecked")
    private static boolean compare(Object a, Object b, IntPredicate expected) {
        return a instanceof Comparable && expected.test(((Comparable) a).compareTo(b));
    }

    private static <V> Predicate<V> lessThan(ToIntFunction<V> getter, NumberColumnFilter filter) {
        return v -> compare(getter.applyAsInt(v), filter.getFilter(), r -> r < 0);
    }

    private static <V> Predicate<V> lessThanOrEqual(ToIntFunction<V> getter, NumberColumnFilter filter) {
        return v -> compare(getter.applyAsInt(v), filter.getFilter(), r -> r <= 0);
    }

    private static <V> Predicate<V> greaterThan(ToIntFunction<V> getter, NumberColumnFilter filter) {
        return v -> compare(getter.applyAsInt(v), filter.getFilter(), r -> r > 0);
    }

    private static <V> Predicate<V> greaterThanOrEqual(ToIntFunction<V> getter, NumberColumnFilter filter) {
        return v -> compare(getter.applyAsInt(v), filter.getFilter(), r -> r >= 0);
    }

    private static <V> Predicate<V> between(ToIntFunction<V> getter, NumberColumnFilter filter) {
        return v -> {
            final int value = getter.applyAsInt(v);
            return compare(value, filter.getFilter(), r -> r >= 0)
                    && compare(value, filter.getFilterTo(), r -> r <= 0);
        };
    }

    private static <V> Predicate<V> str(Function<V, String> getter, Predicate<String> p) {
        return v -> p.test(getter.apply(v));
    }

    private static <V> Predicate<V> contains(Function<V, String> getter, TextColumnFilter filter) {
        return str(getter, s -> s != null && s.contains(filter.getFilter()));
    }

    private static <V> Predicate<V> notContains(Function<V, String> getter, TextColumnFilter filter) {
        return contains(getter, filter).negate();
    }

    private static <V> Predicate<V> startsWith(Function<V, String> getter, TextColumnFilter filter) {
        return str(getter, s -> s != null && s.startsWith(filter.getFilter()));
    }

    private static <V> Predicate<V> endsWith(Function<V, String> getter, TextColumnFilter filter) {
        return str(getter, s -> s != null && s.endsWith(filter.getFilter()));
    }
}
