package com.github.ykiselev.ag.grid.data.types;

import java.util.Comparator;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public interface Attribute<V> {

    String getName();

    Class<?> getType();

    ToIntFunction<V> getIntGetter();

    ToLongFunction<V> getLongGetter();

    ToDoubleFunction<V> getDoubleGetter();

    Function<V, ?> getObjectGetter();

    default Comparator<V> getComparator() {
        Comparator<V> comparator;
        if (getType() == double.class) {
            comparator = Comparator.comparingDouble(getDoubleGetter());
        } else if (getType() == long.class) {
            comparator = Comparator.comparingLong(getLongGetter());
        } else {
            comparator = Comparator.comparing(
                    (Function<V, Comparable>) getObjectGetter(),
                    nullsFirst(naturalOrder())
            );
        }
        return comparator;
    }
}
