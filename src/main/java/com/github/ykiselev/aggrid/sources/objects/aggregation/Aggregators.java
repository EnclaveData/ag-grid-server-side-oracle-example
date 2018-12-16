package com.github.ykiselev.aggrid.sources.objects.aggregation;

import com.github.ykiselev.aggrid.domain.request.AggFunc;
import com.github.ykiselev.aggrid.sources.objects.types.Attribute;
import com.github.ykiselev.aggrid.sources.objects.types.TypeInfo;
import com.google.common.collect.ImmutableMap;

import java.util.Collections;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.DoubleBinaryOperator;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;
import java.util.function.LongBinaryOperator;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collector;

import static java.util.Objects.requireNonNull;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
final class Aggregators {

    private static final Map<AggFunc, Function<Attribute<?>, Accumulator<?>>> INT_AGGREGATORS =
            ImmutableMap.<AggFunc, Function<Attribute<?>, Accumulator<?>>>builder()
                    .put(AggFunc.SUM, a -> summingInt(a.getName(), a.getIntGetter()))
                    .put(AggFunc.AVG, a -> averageInt(a.getName(), a.getIntGetter()))
                    .put(AggFunc.MIN, a -> minInt(a.getName(), a.getIntGetter()))
                    .put(AggFunc.MAX, a -> maxInt(a.getName(), a.getIntGetter()))
                    .build();

    private static final Map<AggFunc, Function<Attribute<?>, Accumulator<?>>> LONG_AGGREGATORS =
            ImmutableMap.<AggFunc, Function<Attribute<?>, Accumulator<?>>>builder()
                    .put(AggFunc.SUM, a -> summingLong(a.getName(), a.getLongGetter()))
                    .put(AggFunc.AVG, a -> averageLong(a.getName(), a.getLongGetter()))
                    .put(AggFunc.MIN, a -> minLong(a.getName(), a.getLongGetter()))
                    .put(AggFunc.MAX, a -> maxLong(a.getName(), a.getLongGetter()))
                    .build();

    private static final Map<AggFunc, Function<Attribute<?>, Accumulator<?>>> DOUBLE_AGGREGATORS =
            ImmutableMap.<AggFunc, Function<Attribute<?>, Accumulator<?>>>builder()
                    .put(AggFunc.SUM, a -> summingDouble(a.getName(), a.getDoubleGetter()))
                    .put(AggFunc.AVG, a -> averageDouble(a.getName(), a.getDoubleGetter()))
                    .put(AggFunc.MIN, a -> minDouble(a.getName(), a.getDoubleGetter()))
                    .put(AggFunc.MAX, a -> maxDouble(a.getName(), a.getDoubleGetter()))
                    .build();

    static <V> Collector<V, ?, Map<String, Object>> createCollector(Map<String, AggFunc> aggFuncs, TypeInfo<V> typeInfo) {
        return Collector.of(
                () -> createMerge(aggFuncs, typeInfo),
                ObjectAggregator::add,
                ObjectAggregator::combine,
                ObjectAggregator::finish
        );
    }

    @SuppressWarnings("unchecked")
    private static <V> Accumulator<V> accumulatorFor(Attribute<V> attr, AggFunc aggFn) {
        final Function<Attribute<?>, Accumulator<?>> function;
        if (attr.getType() == double.class) {
            function = DOUBLE_AGGREGATORS.get(aggFn);
        } else if (attr.getType() == int.class) {
            function = INT_AGGREGATORS.get(aggFn);
        } else if (attr.getType() == long.class) {
            function = LONG_AGGREGATORS.get(aggFn);
        } else {
            function = null;
        }
        if (function == null) {
            throw new IllegalStateException("No aggregation function for " + aggFn + "(" + attr.getType() + ")");
        }
        return (Accumulator<V>) function.apply(attr);
    }

    @SuppressWarnings("unchecked")
    private static <V> ObjectAggregator<V> createMerge(Map<String, AggFunc> columnsToMerge, TypeInfo<V> typeInfo) {
        final BiFunction<String, AggFunc, Accumulator<V>> accumulatorFunction =
                (col, aggFn) ->
                        accumulatorFor(typeInfo.getAttributes().get(col), aggFn);
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

        boolean isEmpty();
    }

    static abstract class AbstractAccumulator<V> implements Accumulator<V> {

        private final String name;

        private boolean empty = true;

        String getName() {
            return name;
        }

        @Override
        public final boolean isEmpty() {
            return empty;
        }

        AbstractAccumulator(String name) {
            this.name = requireNonNull(name);
        }

        @Override
        public final void accumulate(V value) {
            if (empty) {
                accumulateFirst(value);
                empty = false;
            } else {
                accumulateMore(value);
            }
        }

        /**
         * Called from {@link AbstractAccumulator#accumulate} method when this accumulator is non-empty.
         *
         * @param value the value to accumulate.
         */
        abstract void accumulateMore(V value);

        /**
         * Called from {@link AbstractAccumulator#accumulate} method when this accumulator is empty.
         *
         * @param value the value to accumulate.
         */
        abstract void accumulateFirst(V value);

        @Override
        public final void combine(Accumulator<V> other) {
            if (empty) {
                if (!other.isEmpty()) {
                    setFromNonEmpty(other);
                    empty = false;
                }
            } else {
                if (!other.isEmpty()) {
                    combineWithNonEmpty(other);
                }
            }
        }

        /**
         * Called from {@link AbstractAccumulator#combine} method when both this and other accumulators are non empty.
         *
         * @param other the other accumulator of the same type
         */
        abstract void combineWithNonEmpty(Accumulator<V> other);

        /**
         * Called from {@link AbstractAccumulator#combine} method when this accumulator is empty but other is not.
         *
         * @param other the other accumulator of the same type
         */
        abstract void setFromNonEmpty(Accumulator<V> other);
    }

    private static <V> IntAccumulator<V> summingInt(String name, ToIntFunction<V> getter) {
        return new IntAccumulator<>(name, getter, (acc, v) -> acc + v, (acc, count) -> acc);
    }

    private static <V> IntAccumulator<V> averageInt(String name, ToIntFunction<V> getter) {
        return new IntAccumulator<>(name, getter, (acc, v) -> acc + v, (acc, count) -> acc / count);
    }

    private static <V> IntAccumulator<V> minInt(String name, ToIntFunction<V> getter) {
        return new IntAccumulator<>(name, getter, Math::min, (acc, count) -> acc);
    }

    private static <V> IntAccumulator<V> maxInt(String name, ToIntFunction<V> getter) {
        return new IntAccumulator<>(name, getter, Math::max, (acc, count) -> acc);
    }

    static final class IntAccumulator<V> extends AbstractAccumulator<V> {

        private final ToIntFunction<V> getter;

        private final IntBinaryOperator accumulator;

        private final IntBinaryOperator finalizer;

        private int acc;

        IntAccumulator(String name, ToIntFunction<V> getter, IntBinaryOperator accumulator, IntBinaryOperator finalizer) {
            super(name);
            this.getter = requireNonNull(getter);
            this.accumulator = requireNonNull(accumulator);
            this.finalizer = requireNonNull(finalizer);
        }

        @Override
        void accumulateMore(V value) {
            acc = accumulator.applyAsInt(acc, getter.applyAsInt(value));
        }

        @Override
        void accumulateFirst(V value) {
            acc = getter.applyAsInt(value);
        }

        @Override
        void combineWithNonEmpty(Accumulator<V> other) {
            acc = accumulator.applyAsInt(acc, ((IntAccumulator<?>) other).acc);
        }

        @Override
        void setFromNonEmpty(Accumulator<V> other) {
            acc = ((IntAccumulator<?>) other).acc;
        }

        @Override
        public void finish(int count, Map<String, Object> target) {
            if (!isEmpty()) {
                target.put(getName(), finalizer.applyAsInt(acc, count));
            }
        }
    }

    private static <V> LongAccumulator<V> summingLong(String name, ToLongFunction<V> getter) {
        return new LongAccumulator<>(name, getter, (acc, v) -> acc + v, (acc, count) -> acc);
    }

    private static <V> LongAccumulator<V> averageLong(String name, ToLongFunction<V> getter) {
        return new LongAccumulator<>(name, getter, (acc, v) -> acc + v, (acc, count) -> acc / count);
    }

    private static <V> LongAccumulator<V> minLong(String name, ToLongFunction<V> getter) {
        return new LongAccumulator<>(name, getter, Math::min, (acc, count) -> acc);
    }

    private static <V> LongAccumulator<V> maxLong(String name, ToLongFunction<V> getter) {
        return new LongAccumulator<>(name, getter, Math::max, (acc, count) -> acc);
    }

    static final class LongAccumulator<V> extends AbstractAccumulator<V> {

        private final ToLongFunction<V> getter;

        private final LongBinaryOperator accumulator;

        private final LongBinaryOperator finalizer;

        private long acc;

        LongAccumulator(String name, ToLongFunction<V> getter, LongBinaryOperator accumulator, LongBinaryOperator finalizer) {
            super(name);
            this.getter = requireNonNull(getter);
            this.accumulator = requireNonNull(accumulator);
            this.finalizer = requireNonNull(finalizer);
        }

        @Override
        void accumulateFirst(V value) {
            acc = getter.applyAsLong(value);
        }

        @Override
        void accumulateMore(V value) {
            acc = accumulator.applyAsLong(acc, getter.applyAsLong(value));
        }

        @Override
        void setFromNonEmpty(Accumulator<V> other) {
            acc = ((LongAccumulator<?>) other).acc;
        }

        @Override
        void combineWithNonEmpty(Accumulator<V> other) {
            acc = accumulator.applyAsLong(acc, ((LongAccumulator<?>) other).acc);
        }

        @Override
        public void finish(int count, Map<String, Object> target) {
            if (!isEmpty()) {
                target.put(getName(), finalizer.applyAsLong(acc, count));
            }
        }
    }

    private static <V> DoubleAccumulator<V> summingDouble(String name, ToDoubleFunction<V> getter) {
        return new DoubleAccumulator<>(name, getter, (acc, v) -> acc + v, (acc, count) -> acc);
    }

    private static <V> DoubleAccumulator<V> averageDouble(String name, ToDoubleFunction<V> getter) {
        return new DoubleAccumulator<>(name, getter, (acc, v) -> acc + v, (acc, count) -> acc / count);
    }

    private static <V> DoubleAccumulator<V> minDouble(String name, ToDoubleFunction<V> getter) {
        return new DoubleAccumulator<>(name, getter, Math::min, (acc, count) -> acc);
    }

    private static <V> DoubleAccumulator<V> maxDouble(String name, ToDoubleFunction<V> getter) {
        return new DoubleAccumulator<>(name, getter, Math::max, (acc, count) -> acc);
    }

    static final class DoubleAccumulator<V> extends AbstractAccumulator<V> {

        private final ToDoubleFunction<V> getter;

        private final DoubleBinaryOperator accumulator;

        private final DoubleBinaryOperator finalizer;

        private double acc;

        private boolean empty = true;

        DoubleAccumulator(String name, ToDoubleFunction<V> getter, DoubleBinaryOperator accumulator, DoubleBinaryOperator finalizer) {
            super(name);
            this.getter = requireNonNull(getter);
            this.accumulator = requireNonNull(accumulator);
            this.finalizer = requireNonNull(finalizer);
        }

        @Override
        void accumulateFirst(V value) {
            acc = getter.applyAsDouble(value);
        }

        @Override
        void accumulateMore(V value) {
            acc = accumulator.applyAsDouble(acc, getter.applyAsDouble(value));
        }

        @Override
        void setFromNonEmpty(Accumulator<V> other) {
            acc = ((DoubleAccumulator<?>) other).acc;
        }

        @Override
        void combineWithNonEmpty(Accumulator<V> other) {
            acc = accumulator.applyAsDouble(acc, ((DoubleAccumulator<?>) other).acc);
        }

        @Override
        public void finish(int count, Map<String, Object> target) {
            if (!isEmpty()) {
                target.put(getName(), finalizer.applyAsDouble(acc, count));
            }
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
            if (first == null) {
                return Collections.emptyMap();
            }
            final Map<String, Object> result = toMap.apply(first);
            for (Accumulator<V> aggregator : accumulators) {
                aggregator.finish(counter, result);
            }
            return result;
        }
    }
}