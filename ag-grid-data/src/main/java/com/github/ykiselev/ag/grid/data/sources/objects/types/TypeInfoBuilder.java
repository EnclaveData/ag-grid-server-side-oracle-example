package com.github.ykiselev.ag.grid.data.sources.objects.types;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

import static java.util.Objects.requireNonNull;

/**
 * @author Yuriy Kiselev (uze@yandex.ru)
 * @since 2018-12-22
 */
public final class TypeInfoBuilder<V> {

    private final List<Attribute<V>> attributes = new ArrayList<>();

    public TypeInfoBuilder<V> withInt(String name, ToIntFunction<V> getter) {
        attributes.add(new IntAttribute<>(name, getter));
        return this;
    }

    public TypeInfoBuilder<V> withLong(String name, ToLongFunction<V> getter) {
        attributes.add(new LongAttribute<>(name, getter));
        return this;
    }

    public TypeInfoBuilder<V> withDouble(String name, ToDoubleFunction<V> getter) {
        attributes.add(new DoubleAttribute<>(name, getter));
        return this;
    }

    public TypeInfoBuilder<V> with(String name, Class<?> clazz, Function<V, ?> getter) {
        attributes.add(new ObjectAttribute<>(name, clazz, getter));
        return this;
    }

    public <O> ChainBuilder<O> withChain(String name, Function<V, O> getter) {
        return new ChainBuilder<>(name, getter);
    }

    public TypeInfo<V> build() {
        return new DefaultTypeInfo<>(attributes);
    }

    public final class ChainBuilder<O> {

        final String name;

        final Function<V, O> getter;

        ChainBuilder(String name, Function<V, O> getter) {
            this.name = requireNonNull(name);
            this.getter = requireNonNull(getter);
        }

        public <O2> ChainBuilder<O2> and(Function<O, O2> getter) {
            return new ChainBuilder<>(name, this.getter.andThen(getter));
        }

        /**
         * Adds integer attribute to type info builder.
         *
         * @param getter the last getter in chain
         * @return the type info builder
         */
        public TypeInfoBuilder<V> andInt(ToIntFunction<O> getter) {
            attributes.add(new IntAttribute<>(name, v ->
                    getter.applyAsInt(this.getter.apply(v))));
            return TypeInfoBuilder.this;
        }

        /**
         * Adds long attribute to type info builder.
         *
         * @param getter the last getter in chain
         * @return the type info builder
         */
        public TypeInfoBuilder<V> andLong(ToLongFunction<O> getter) {
            attributes.add(new LongAttribute<V>(name, v ->
                    getter.applyAsLong(this.getter.apply(v))));
            return TypeInfoBuilder.this;
        }

        /**
         * Adds double attribute to type info builder.
         *
         * @param getter the last getter in chain
         * @return the type info builder
         */
        public TypeInfoBuilder<V> andDouble(ToDoubleFunction<O> getter) {
            attributes.add(new DoubleAttribute<>(name, v ->
                    getter.applyAsDouble(this.getter.apply(v))));
            return TypeInfoBuilder.this;
        }

        /**
         * Adds object attribute to type info builder.
         *
         * @param getter the last getter in chain
         * @return the type info builder
         */
        public TypeInfoBuilder<V> andObject(Class<?> clazz, Function<O, ?> getter) {
            attributes.add(new ObjectAttribute<>(name, clazz,
                    v -> getter.apply(this.getter.apply(v))));
            return TypeInfoBuilder.this;
        }
    }
}
