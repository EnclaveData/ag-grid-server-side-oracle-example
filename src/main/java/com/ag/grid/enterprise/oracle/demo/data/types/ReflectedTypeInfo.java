package com.ag.grid.enterprise.oracle.demo.data.types;

import com.google.common.collect.ImmutableMap;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public final class ReflectedTypeInfo<T> implements TypeInfo {

    private final Map<String, Field> fields;

    public ReflectedTypeInfo(Map<String, Field> fields) {
        this.fields = requireNonNull(fields);
    }

    @Override
    public Set<String> getNames() {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public Class<?> getType(String name) {
        throw new UnsupportedOperationException("not implemented");
    }

    public static <T> TypeInfo of(Class<T> clazz) {
        return new ReflectedTypeInfo<T>(
                ImmutableMap.copyOf(
                        Arrays.stream(clazz.getDeclaredFields())
                                .collect(Collectors.toMap(
                                        Field::getName,
                                        Function.identity()
                                ))
                )
        );
    }
}
