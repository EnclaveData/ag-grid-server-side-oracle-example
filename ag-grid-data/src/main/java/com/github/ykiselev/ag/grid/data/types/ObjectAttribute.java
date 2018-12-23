package com.github.ykiselev.ag.grid.data.types;

import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

import static java.util.Objects.requireNonNull;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public final class ObjectAttribute<V> extends AbstractAttribute<V> {

    private final Function<V, ?> getter;

    public ObjectAttribute(String name, Class<?> type, Function<V, ?> getter) {
        super(name, type);
        this.getter = requireNonNull(getter);
    }

    @Override
    public ToIntFunction<V> getIntGetter() {
        return v -> (Integer) getter.apply(v);
    }

    @Override
    public ToLongFunction<V> getLongGetter() {
        return v -> (Long) getter.apply(v);
    }

    @Override
    public ToDoubleFunction<V> getDoubleGetter() {
        return v -> (Double) getter.apply(v);
    }

    @Override
    public Function<V, ?> getObjectGetter() {
        return getter;
    }
}
