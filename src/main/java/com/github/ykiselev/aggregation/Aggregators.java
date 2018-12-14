package com.github.ykiselev.aggregation;

import com.ag.grid.enterprise.oracle.demo.data.Context;
import com.ag.grid.enterprise.oracle.demo.data.objectsource.ObjectSource;
import com.ag.grid.enterprise.oracle.demo.data.types.Attribute;
import com.ag.grid.enterprise.oracle.demo.data.types.TypeInfo;
import com.ag.grid.enterprise.oracle.demo.request.AggFunc;
import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.DoubleBinaryOperator;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;
import java.util.function.LongBinaryOperator;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
final class Aggregators {

    private static final Map<AggregationKey, Function<Attribute<?>, Accumulator<?>>> AGGREGATORS =
            ImmutableMap.<AggregationKey, Function<Attribute<?>, Accumulator<?>>>builder()
                    .put(new AggregationKey(int.class, AggFunc.SUM), a -> summingInt(a.getName(), a.getIntGetter()))
                    .put(new AggregationKey(int.class, AggFunc.AVG), a -> averageInt(a.getName(), a.getIntGetter()))
                    .put(new AggregationKey(int.class, AggFunc.MIN), a -> minInt(a.getName(), a.getIntGetter()))
                    .put(new AggregationKey(int.class, AggFunc.MAX), a -> maxInt(a.getName(), a.getIntGetter()))
                    .put(new AggregationKey(long.class, AggFunc.SUM), a -> summingLong(a.getName(), a.getLongGetter()))
                    .put(new AggregationKey(long.class, AggFunc.AVG), a -> averageLong(a.getName(), a.getLongGetter()))
                    .put(new AggregationKey(long.class, AggFunc.MIN), a -> minLong(a.getName(), a.getLongGetter()))
                    .put(new AggregationKey(long.class, AggFunc.MAX), a -> maxLong(a.getName(), a.getLongGetter()))
                    .put(new AggregationKey(double.class, AggFunc.SUM), a -> summingDouble(a.getName(), a.getDoubleGetter()))
                    .put(new AggregationKey(double.class, AggFunc.AVG), a -> averageDouble(a.getName(), a.getDoubleGetter()))
                    .put(new AggregationKey(double.class, AggFunc.MIN), a -> minDouble(a.getName(), a.getDoubleGetter()))
                    .put(new AggregationKey(double.class, AggFunc.MAX), a -> maxDouble(a.getName(), a.getDoubleGetter()))
                    .build();

    static <K, V> Collector<V, ?, Map<String, Object>> createCollector(Context context, ObjectSource<K, V> source) {
        final Map<String, AggFunc> aggColumns = context.getColumnsToMerge()
                .stream()
                .collect(Collectors.toMap(
                        Function.identity(),
                        col -> context.getColumn(col).getAggFunc()
                ));

        return Collector.of(
                () -> createMerge(aggColumns, source.getTypeInfo()),
                ObjectAggregator::add,
                ObjectAggregator::combine,
                ObjectAggregator::finish
        );
    }

    @SuppressWarnings("unchecked")
    static <V> ObjectAggregator<V> createMerge(Map<String, AggFunc> columnsToMerge, TypeInfo<V> typeInfo) {
        final BiFunction<String, AggFunc, Accumulator<V>> accumulatorFunction =
                (col, aggFn) -> {
                    final Attribute<V> attr = typeInfo.getAttributes().get(col);
                    final AggregationKey key = new AggregationKey(attr.getType(), aggFn);
                    final Function<Attribute<?>, Accumulator<?>> function = AGGREGATORS.get(key);
                    if (function == null) {
                        throw new IllegalStateException("No aggregation function for " + key);
                    }
                    return (Accumulator<V>) function.apply(attr);
                };
        return new ObjectAggregator<>(
                typeInfo.toMap(),
                columnsToMerge.entrySet()
                        .stream()
                        .map(e -> accumulatorFunction.apply(e.getKey(), e.getValue()))
                        .toArray(Accumulator[]::new)
        );
    }

    interface Accumulator<V> {

        void accumulate(V value);

        void combine(Accumulator<V> other);

        void finish(int count, Map<String, Object> target);
    }

    static <V> IntAccumulator<V> summingInt(String name, ToIntFunction<V> getter) {
        return new IntAccumulator<>(name, getter, (acc, v) -> acc + v, (acc, count) -> acc);
    }

    static <V> IntAccumulator<V> averageInt(String name, ToIntFunction<V> getter) {
        return new IntAccumulator<>(name, getter, (acc, v) -> acc + v, (acc, count) -> acc / count);
    }

    static <V> IntAccumulator<V> minInt(String name, ToIntFunction<V> getter) {
        return new IntAccumulator<>(name, getter, Math::min, (acc, count) -> acc);
    }

    static <V> IntAccumulator<V> maxInt(String name, ToIntFunction<V> getter) {
        return new IntAccumulator<>(name, getter, Math::max, (acc, count) -> acc);
    }

    static final class IntAccumulator<V> implements Accumulator<V> {

        private final String name;

        private final ToIntFunction<V> getter;

        private final IntBinaryOperator accumulator;

        private final IntBinaryOperator finalizer;

        private int acc;

        IntAccumulator(String name, ToIntFunction<V> getter, IntBinaryOperator accumulator, IntBinaryOperator finalizer) {
            this.name = requireNonNull(name);
            this.getter = requireNonNull(getter);
            this.accumulator = requireNonNull(accumulator);
            this.finalizer = requireNonNull(finalizer);
        }

        @Override
        public void accumulate(V value) {
            acc = accumulator.applyAsInt(acc, getter.applyAsInt(value));
        }

        @Override
        public void combine(Accumulator<V> other) {
            acc = accumulator.applyAsInt(acc, ((IntAccumulator<?>) other).acc);
        }

        @Override
        public void finish(int count, Map<String, Object> target) {
            target.put(name, finalizer.applyAsInt(acc, count));
        }
    }

    static <V> LongAccumulator<V> summingLong(String name, ToLongFunction<V> getter) {
        return new LongAccumulator<>(name, getter, (acc, v) -> acc + v, (acc, count) -> acc);
    }

    static <V> LongAccumulator<V> averageLong(String name, ToLongFunction<V> getter) {
        return new LongAccumulator<>(name, getter, (acc, v) -> acc + v, (acc, count) -> acc / count);
    }

    static <V> LongAccumulator<V> minLong(String name, ToLongFunction<V> getter) {
        return new LongAccumulator<>(name, getter, Math::min, (acc, count) -> acc);
    }

    static <V> LongAccumulator<V> maxLong(String name, ToLongFunction<V> getter) {
        return new LongAccumulator<>(name, getter, Math::max, (acc, count) -> acc);
    }

    static final class LongAccumulator<V> implements Accumulator<V> {

        private final String name;

        private final ToLongFunction<V> getter;

        private final LongBinaryOperator accumulator;

        private final LongBinaryOperator finalizer;

        private long acc;

        LongAccumulator(String name, ToLongFunction<V> getter, LongBinaryOperator accumulator, LongBinaryOperator finalizer) {
            this.name = requireNonNull(name);
            this.getter = requireNonNull(getter);
            this.accumulator = requireNonNull(accumulator);
            this.finalizer = requireNonNull(finalizer);
        }

        @Override
        public void accumulate(V value) {
            acc = accumulator.applyAsLong(acc, getter.applyAsLong(value));
        }

        @Override
        public void combine(Accumulator<V> other) {
            acc = accumulator.applyAsLong(acc, ((LongAccumulator<?>) other).acc);
        }

        @Override
        public void finish(int count, Map<String, Object> target) {
            target.put(name, finalizer.applyAsLong(acc, count));
        }
    }

    static <V> DoubleAccumulator<V> summingDouble(String name, ToDoubleFunction<V> getter) {
        return new DoubleAccumulator<>(name, getter, (acc, v) -> acc + v, (acc, count) -> acc);
    }

    static <V> DoubleAccumulator<V> averageDouble(String name, ToDoubleFunction<V> getter) {
        return new DoubleAccumulator<>(name, getter, (acc, v) -> acc + v, (acc, count) -> acc / count);
    }

    static <V> DoubleAccumulator<V> minDouble(String name, ToDoubleFunction<V> getter) {
        return new DoubleAccumulator<>(name, getter, Math::min, (acc, count) -> acc);
    }

    static <V> DoubleAccumulator<V> maxDouble(String name, ToDoubleFunction<V> getter) {
        return new DoubleAccumulator<>(name, getter, Math::max, (acc, count) -> acc);
    }

    static final class DoubleAccumulator<V> implements Accumulator<V> {

        private final String name;

        private final ToDoubleFunction<V> getter;

        private final DoubleBinaryOperator accumulator;

        private final DoubleBinaryOperator finalizer;

        private double acc;

        DoubleAccumulator(String name, ToDoubleFunction<V> getter, DoubleBinaryOperator accumulator, DoubleBinaryOperator finalizer) {
            this.name = requireNonNull(name);
            this.getter = requireNonNull(getter);
            this.accumulator = requireNonNull(accumulator);
            this.finalizer = requireNonNull(finalizer);
        }

        @Override
        public void accumulate(V value) {
            acc = accumulator.applyAsDouble(acc, getter.applyAsDouble(value));
        }

        @Override
        public void combine(Accumulator<V> other) {
            acc = accumulator.applyAsDouble(acc, ((DoubleAccumulator<?>) other).acc);
        }

        @Override
        public void finish(int count, Map<String, Object> target) {
            target.put(name, finalizer.applyAsDouble(acc, count));
        }
    }

    /**
     *
     */
    static final class ObjectAggregator<V> {

        private final Function<V, Map<String, Object>> toMap;

        private final Accumulator<V>[] accumulators;

        private V first;

        private int counter;

        ObjectAggregator(Function<V, Map<String, Object>> toMap, Accumulator<V>[] accumulators) {
            this.toMap = requireNonNull(toMap);
            this.accumulators = accumulators.clone();
        }

        void add(V value) {
            if (first == null) {
                first = value;
            }
            for (Accumulator<V> aggregator : accumulators) {
                aggregator.accumulate(value);
            }
            counter++;
        }

        ObjectAggregator<V> combine(ObjectAggregator<V> other) {
            if (first == null) {
                return other;
            }
            if (other.first != null) {
                if (accumulators.length != other.accumulators.length) {
                    throw new IllegalStateException("Accumulator arrays size mismatch: " + accumulators.length + " <> " + other.accumulators.length);
                }
                for (int i = 0; i < accumulators.length; i++) {
                    accumulators[i].combine(other.accumulators[i]);
                }
            }
            return this;
        }

        Map<String, Object> finish() {
            final Map<String, Object> result = toMap.apply(first);
            for (Accumulator<V> aggregator : accumulators) {
                aggregator.finish(counter, result);
            }
            return result;
        }
    }

    private static final class AggregationKey {

        private final Class<?> clazz;

        private final AggFunc aggFunc;

        AggregationKey(Class<?> clazz, AggFunc aggFunc) {
            this.clazz = requireNonNull(clazz);
            this.aggFunc = requireNonNull(aggFunc);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AggregationKey that = (AggregationKey) o;
            return clazz.equals(that.clazz) &&
                    aggFunc == that.aggFunc;
        }

        @Override
        public int hashCode() {
            return Objects.hash(clazz, aggFunc);
        }

        @Override
        public String toString() {
            return "AggregationKey{" +
                    "clazz=" + clazz +
                    ", aggFunc=" + aggFunc +
                    '}';
        }
    }
}