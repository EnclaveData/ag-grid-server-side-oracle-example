package com.github.ykiselev.aggregation;

import java.util.Map;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public interface Accumulator<V> {

    void accumulate(V value);

    void combine(Accumulator<V> other);

    void finish(int count, Map<String,Object> target);
}
