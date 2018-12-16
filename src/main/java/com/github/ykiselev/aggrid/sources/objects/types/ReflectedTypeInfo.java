package com.github.ykiselev.aggrid.sources.objects.types;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
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

    /**
     * Creates instance of {@link TypeInfo} populated with attributes of specified class.
     *
     * @param clazz the class to create type info for.
     * @param <T>   the type parameter.
     * @return the type info.
     */
    public static <T> TypeInfo<T> of(Class<T> clazz) {
        return new DefaultTypeInfo<>(
                attributesOf(clazz)
        );
    }

    /**
     * Builds map of attributes for a given class.
     *
     * @param clazz the class to build attributes for
     * @param <T>   the type parameter
     * @return the map of attribute names mapped to attributes
     */
    public static <T> Map<String, Attribute<T>> attributesOf(Class<T> clazz) {
        final Predicate<PropertyDescriptor> filter = p ->
                !"class".equals(p.getName());
        final Function<PropertyDescriptor, Attribute<T>> mapping = p ->
                new BeanAttribute<>(
                        p.getName(),
                        p.getPropertyType(),
                        p.getReadMethod()
                );
        try {
            return Arrays.stream(Introspector.getBeanInfo(clazz).getPropertyDescriptors())
                    .filter(filter)
                    .map(mapping)
                    .collect(Collectors.toMap(
                            Attribute::getName,
                            Function.identity()
                    ));
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

        private <I, R> R invoke(V instance, Function<I, R> mapper) {
            try {
                @SuppressWarnings("unchecked") final I value = (I) getter.invoke(instance);
                return value != null ? mapper.apply(value) : null;
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }
}