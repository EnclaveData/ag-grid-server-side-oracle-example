package com.github.ykiselev.ag.grid.data.types;

import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

/**
 * @author Yuriy Kiselev (uze@yandex.ru)
 * @since 18.04.2019
 */
public final class TupleAttribute extends AbstractAttribute<Object[]> {

    private final int index;

    public TupleAttribute(String name, Class<?> type, int index) {
        super(name, type);
        this.index = index;
    }

    @Override
    public ToIntFunction<Object[]> getIntGetter() {
        return v -> (Integer) v[index];
    }

    @Override
    public ToLongFunction<Object[]> getLongGetter() {
        return v -> (Long) v[index];
    }

    @Override
    public ToDoubleFunction<Object[]> getDoubleGetter() {
        return v -> (Double) v[index];
    }

    @Override
    public Function<Object[], ?> getObjectGetter() {
        return v -> v[index];
    }
}
