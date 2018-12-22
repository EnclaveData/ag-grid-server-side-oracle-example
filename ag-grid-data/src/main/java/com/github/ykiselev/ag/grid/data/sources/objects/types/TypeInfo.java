package com.github.ykiselev.ag.grid.data.sources.objects.types;

import java.util.Map;
import java.util.function.Function;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public interface TypeInfo<V> {

    Map<String, Attribute<V>> getAttributes();

    Function<V,Map<String,Object>> toMap();
}
