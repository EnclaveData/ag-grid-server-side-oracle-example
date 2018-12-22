package com.github.ykiselev.ag.grid.data.sources.objects.types;

import com.google.common.collect.ImmutableMap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public final class DefaultTypeInfo<V> implements TypeInfo<V> {

    private final Map<String, Attribute<V>> attributes;

    public DefaultTypeInfo(Map<String, Attribute<V>> attributes) {
        this.attributes = ImmutableMap.copyOf(attributes);
    }

    public DefaultTypeInfo(Collection<Attribute<V>> attributes) {
        this.attributes = ImmutableMap.copyOf(
                attributes.stream()
                        .collect(Collectors.toMap(
                                Attribute::getName,
                                a -> a
                        ))
        );
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
