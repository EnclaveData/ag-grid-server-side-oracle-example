package com.github.ykiselev.ag.grid.data.types;

import com.google.common.collect.ImmutableMap;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public final class DefaultTypeInfo<V> implements TypeInfo<V> {

    private final Supplier<Map<String, Object>> mapFactory;

    private final Map<String, Attribute<V>> attributes;

    public DefaultTypeInfo(Map<String, Attribute<V>> attributes) {
        this(attributes, HashMap::new);
    }

    public DefaultTypeInfo(Map<String, Attribute<V>> attributes, Supplier<Map<String, Object>> mapFactory) {
        this.attributes = ImmutableMap.copyOf(attributes);
        this.mapFactory = requireNonNull(mapFactory);
    }

    public DefaultTypeInfo(Collection<Attribute<V>> attributes) {
        this(attributes, HashMap::new);
    }

    public DefaultTypeInfo(Collection<Attribute<V>> attributes, Supplier<Map<String, Object>> mapFactory) {
        this(ImmutableMap.copyOf(
                attributes.stream()
                        .collect(Collectors.toMap(
                                Attribute::getName,
                                a -> a
                        ))
        ), mapFactory);
    }

    @Override
    public boolean hasName(String name) {
        return attributes.containsKey(name);
    }

    @Override
    public Attribute<V> getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    public Function<V, Map<String, Object>> toMap(Collection<String> names) {
        return value -> {
            final Map<String, Object> result = mapFactory.get();
            final Collection<String> namesTiUse;
            if (names.isEmpty()) {
                namesTiUse = attributes.keySet();
            } else {
                namesTiUse = names;
            }
            namesTiUse.stream()
                    .map(attributes::get)
                    .forEach(attr ->
                            result.put(attr.getName(), attr.getObjectGetter().apply(value)));
            return result;
        };
    }
}
