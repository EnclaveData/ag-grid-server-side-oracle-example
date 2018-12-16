package com.github.ykiselev.aggrid.sources.objects.types;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public final class ReflectedTypeInfo {

    public static <T> TypeInfo<T> of(Class<T> clazz) {
        final Predicate<PropertyDescriptor> filter = p ->
                !"class".equals(p.getName());
        final Function<PropertyDescriptor, Attribute<T>> mapping = p ->
                new BeanAttribute<>(
                        p.getName(),
                        p.getPropertyType(),
                        p.getReadMethod()
                );
        try {
            return new DefaultTypeInfo<>(
                    Arrays.stream(Introspector.getBeanInfo(clazz).getPropertyDescriptors())
                            .filter(filter)
                            .map(mapping)
                            .collect(Collectors.toMap(
                                    Attribute::getName,
                                    Function.identity()
                            ))
            );
        } catch (IntrospectionException e) {
            throw new IllegalArgumentException("Unable to create type info!", e);
        }
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