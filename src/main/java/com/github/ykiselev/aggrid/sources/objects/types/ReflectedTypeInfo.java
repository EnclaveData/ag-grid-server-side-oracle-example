package com.github.ykiselev.aggrid.sources.objects.types;

import com.google.common.collect.ImmutableMap;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public final class ReflectedTypeInfo<V> implements TypeInfo<V> {

    private final Map<String, Attribute<V>> attributes;

    public ReflectedTypeInfo(Map<String, Attribute<V>> attributes) {
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

    @SuppressWarnings("unchecked")
    public static <T> TypeInfo<T> of(Class<T> clazz) {
        try {
            return new ReflectedTypeInfo<T>(
                    Arrays.stream(Introspector.getBeanInfo(clazz).getPropertyDescriptors())
                            .map(ReflectedTypeInfo::toAttribute)
                            .map(a -> (Attribute<T>) a)
                            .collect(Collectors.toMap(
                                    Attribute::getName,
                                    Function.identity()
                            ))
            );
        } catch (IntrospectionException e) {
            throw new IllegalArgumentException("Unable to create type info!", e);
        }
    }

    private static Attribute<?> toAttribute(PropertyDescriptor d) {
        return new BeanAttribute<>(
                d.getName(),
                d.getPropertyType(),
                d.getReadMethod()
        );
    }

    private static final class BeanAttribute<V> implements Attribute<V> {

        private final String name;

        private final Class<?> type;

        private final Method getter;

        BeanAttribute(String name, Class<?> type, Method getter) {
            this.name = requireNonNull(name);
            this.type = requireNonNull(type);
            this.getter = requireNonNull(getter);
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Class<?> getType() {
            return type;
        }

        @Override
        public ToIntFunction<V> getIntGetter() {
            return v -> invoke(v, Number::intValue);
        }

        @Override
        public ToLongFunction<V> getLongGetter() {
            return v -> invoke(v, Number::longValue);
        }

        @Override
        public ToDoubleFunction<V> getDoubleGetter() {
            return v -> invoke(v, Number::doubleValue);
        }

        @Override
        public Function<V, ?> getObjectGetter() {
            return v -> invoke(v, Function.identity());
        }

        @SuppressWarnings("unchecked")
        private <I, R> R invoke(V instance, Function<I, R> mapper) {
            final Object raw;
            try {
                raw = getter.invoke(instance);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
            return raw != null ? mapper.apply((I) raw) : null;
        }
    }
}
