package com.github.ykiselev.ag.grid.data.sources.objects.types;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public interface TypeInfo<V> {

    Set<String> getNames();

    boolean hasName(String name);

    Attribute<V> getAttribute(String name);

    Function<V, Map<String, Object>> toMap();
}
