package com.github.ykiselev.ag.grid.data.types;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public interface TypeInfo<V> {

    boolean hasName(String name);

    Attribute<V> getAttribute(String name);

    Function<V, Map<String, Object>> toMap(Collection<String> names);

    default Function<V, Map<String, Object>> toMap() {
        return toMap(Collections.emptyList());
    }
}
