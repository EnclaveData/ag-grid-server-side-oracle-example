package com.ag.grid.enterprise.oracle.demo.data.objectsource;

import com.ag.grid.enterprise.oracle.demo.Trade;
import org.junit.Test;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public class AggregationTest {

    @Test
    public void shouldAggregate2() {

    }

    @Test
    public void shouldAggregate() {
        assertEquals("a", Stream.of("a").collect(Collectors.reducing((a, b) -> a + b)).orElse("x"));
    }

    @Test
    public void shouldAggregateValues() {
        assertEquals("a", Stream.of("a").collect(Collectors.collectingAndThen(Collectors.reducing((a, b) -> a + b), o -> o.orElse(null))));
    }

    @Test
    public void shouldAggregateEmptyValues() {
        assertNull(Stream.of().collect(Collectors.collectingAndThen(Collectors.reducing((a, b) -> "xxx"), o -> o.orElse(null))));
    }

    @Test
    public void shouldReduce() {
        Trade t1 = new Trade();
        t1.setValue(2);
        Trade t2 = new Trade();
        t2.setValue(5);
        Trade r = Stream.of(t1, t2).collect(reduce());
        assertEquals(3.5d, r.getValue(), 0.0001d);
    }

    private Collector<Trade, ?, Trade> reduce() {
        return Collector.of(
                Aggregator::new,
                Aggregator::add,
                Aggregator::combine,
                Aggregator::finish,
                Collector.Characteristics.UNORDERED
        );
    }

    /**
     * sum = sum
     * avg = sum / count
     * min = min value
     * max = max value
     */
    private static class Aggregator {

        private Trade acc;

        private int counter;

        void add(Trade value) {
            if (acc == null) {
                acc = value;
            } else {
                acc.setValue(acc.getValue() + value.getValue());
            }
            counter++;
        }

        Aggregator combine(Aggregator other) {
            if (acc == null) {
                return other;
            }
            if (other.acc != null) {
                acc.setValue(acc.getValue() + other.acc.getValue());
                counter += other.counter;
            }
            return this;
        }

        Trade finish() {
            acc.setValue(acc.getValue() / (double) counter);
            return acc;
        }
    }
}