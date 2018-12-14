package com.github.ykiselev.aggrid.sources.objects.types;

import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

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

}
