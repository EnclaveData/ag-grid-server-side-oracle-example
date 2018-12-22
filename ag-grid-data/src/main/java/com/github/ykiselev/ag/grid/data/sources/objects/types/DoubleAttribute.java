package com.github.ykiselev.ag.grid.data.sources.objects.types;

import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

import static java.util.Objects.requireNonNull;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public final class DoubleAttribute<V> extends AbstractAttribute<V> {

    private final ToDoubleFunction<V> getter;

    public DoubleAttribute(String name, ToDoubleFunction<V> getter) {
        super(name, double.class);
        this.getter = requireNonNull(getter);
    }

    @Override
    public ToIntFunction<V> getIntGetter() {
        return v -> Math.toIntExact(getLongGetter().applyAsLong(v));
    }

    @Override
    public ToLongFunction<V> getLongGetter() {
        return v -> {
            final double value = getter.applyAsDouble(v);
            if (value <= Long.MIN_VALUE || value >= Long.MAX_VALUE) {
                throw new ArithmeticException("long overflow");
            }
            return (long) value;
        };
    }

    @Override
    public ToDoubleFunction<V> getDoubleGetter() {
        return getter;
    }

    @Override
    public Function<V, ?> getObjectGetter() {
        return getter::applyAsDouble;
    }
}
