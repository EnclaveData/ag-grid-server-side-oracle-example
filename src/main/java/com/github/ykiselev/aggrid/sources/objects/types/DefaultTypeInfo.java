package com.github.ykiselev.aggrid.sources.objects.types;

import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public final class DefaultTypeInfo<V> implements TypeInfo<V> {

    private final Map<String, Attribute<V>> attributes;

    public DefaultTypeInfo(Map<String, Attribute<V>> attributes) {
        this.attributes = ImmutableMap.copyOf(attributes);
    }

    @Override
    public Map<String, Attribute<V>> getAttributes() {
        return attributes;
    }

    @Override
    public Function<V, Map<String, Object>> toMap() {
        return value -> {
            final Map<String, Object> result = new HashMap<>();
            attributes.forEach((name, attr) ->
                    result.put(name, attr.getObjectGetter().apply(value)));
            return result;
        };
    }
}
