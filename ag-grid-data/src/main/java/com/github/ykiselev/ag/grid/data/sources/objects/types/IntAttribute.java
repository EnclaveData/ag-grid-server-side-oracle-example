package com.github.ykiselev.ag.grid.data.sources.objects.types;

import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

import static java.util.Objects.requireNonNull;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public final class IntAttribute<V> extends AbstractAttribute<V> {

    private final ToIntFunction<V> getter;

    public IntAttribute(String name, ToIntFunction<V> getter) {
        super(name, int.class);
        this.getter = requireNonNull(getter);
    }

    @Override
    public ToIntFunction<V> getIntGetter() {
        return getter;
    }

    @Override
    public ToLongFunction<V> getLongGetter() {
        return getter::applyAsInt;
    }

    @Override
    public ToDoubleFunction<V> getDoubleGetter() {
        return getter::applyAsInt;
    }

    @Override
    public Function<V, ?> getObjectGetter() {
        return getter::applyAsInt;
    }
}
