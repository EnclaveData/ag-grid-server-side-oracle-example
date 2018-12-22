package com.github.ykiselev.ag.grid.data.sources.objects.types;

import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

import static java.util.Objects.requireNonNull;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public final class LongAttribute<V> extends AbstractAttribute<V> {

    private final ToLongFunction<V> getter;

    public LongAttribute(String name, ToLongFunction<V> getter) {
        super(name, long.class);
        this.getter = requireNonNull(getter);
    }

    @Override
    public ToIntFunction<V> getIntGetter() {
        return v -> Math.toIntExact(getter.applyAsLong(v));
    }

    @Override
    public ToLongFunction<V> getLongGetter() {
        return getter;
    }

    @Override
    public ToDoubleFunction<V> getDoubleGetter() {
        return getter::applyAsLong;
    }

    @Override
    public Function<V, ?> getObjectGetter() {
        return getter::applyAsLong;
    }
}
