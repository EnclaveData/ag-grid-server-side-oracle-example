package com.github.ykiselev.ag.grid.data.aggregation

import com.github.ykiselev.ag.grid.api.request.AggFunc
import com.github.ykiselev.ag.grid.data.types.DefaultTypeInfo
import com.github.ykiselev.ag.grid.data.types.DoubleAttribute
import com.github.ykiselev.ag.grid.data.types.IntAttribute
import com.github.ykiselev.ag.grid.data.types.LongAttribute
import spock.lang.Specification

import java.util.stream.Stream

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
class ObjectMergeTest extends Specification {

    def collector = ObjectMerge.createCollector(
            [
                    'a': AggFunc.MAX,
                    'b': AggFunc.MIN,
                    'c': AggFunc.AVG,
                    'd': AggFunc.SUM,
                    'e': AggFunc.MAX,
                    'f': AggFunc.MIN,
                    'g': AggFunc.AVG,
                    'h': AggFunc.SUM,
                    'i': AggFunc.MAX,
                    'j': AggFunc.MIN,
                    'k': AggFunc.AVG,
                    'l': AggFunc.SUM
            ],
            new DefaultTypeInfo<Object[]>(
                    [
                            new IntAttribute<>('a', { v -> (int) v[0] }),
                            new IntAttribute<>('b', { v -> (int) v[1] }),
                            new IntAttribute<>('c', { v -> (int) v[2] }),
                            new IntAttribute<>('d', { v -> (int) v[3] }),
                            new LongAttribute<>('e', { v -> (long) v[4] }),
                            new LongAttribute<>('f', { v -> (long) v[5] }),
                            new LongAttribute<>('g', { v -> (long) v[6] }),
                            new LongAttribute<>('h', { v -> (long) v[7] }),
                            new DoubleAttribute('i', { v -> (double) v[8] }),
                            new DoubleAttribute('j', { v -> (double) v[9] }),
                            new DoubleAttribute('k', { v -> (double) v[10] }),
                            new DoubleAttribute('l', { v -> (double) v[11] })
                    ].collectEntries {
                        [(it.getName()): it]
                    }
            )
    )

    def "should aggregate ints, longs and doubles"() {
        when:
        def r = Stream.of(
                [1, 2, 3, 4, 5L, 6L, 7L, 8L, 9d, 10d, 11d, 12d],
                [13, 14, 15, 16, 17L, 18L, 19L, 20L, 21d, 22d, 23d, 24d],
                [25, 26, 27, 28, 29L, 30L, 31L, 32L, 33d, 34d, 35d, 36d]
        ).collect(collector)

        then:
        r instanceof Map
        r.a == 25
        r.b == 2
        r.c == 15
        r.d == 48
        r.e == 29L
        r.f == 6L
        r.g == 19L
        r.h == 60L
        r.i == 33d
        r.j == 10d
        r.k == 23d
        r.l == 72d
    }

    def "should support empty stream"() {
        when:
        def r = Stream.of().collect(collector)

        then:
        r instanceof Map
    }

}
