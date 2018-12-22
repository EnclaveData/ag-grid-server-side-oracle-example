package com.github.ykiselev.ag.grid.data.sources.objects.types;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public class DoubleAttributeTest {

    private final Attribute<Object> attr = new DoubleAttribute<>("z", v -> (double) v);

    @Test
    public void shouldReturnName() {
        assertEquals("z", attr.getName());
    }

    @Test
    public void shouldReturnType() {
        assertEquals(double.class, attr.getType());
    }

    @Test
    public void shouldGetInt() {
        assertEquals(123, attr.getIntGetter().applyAsInt(123d));
    }

    @Test(expected = ArithmeticException.class)
    public void shouldThrowIfTooBigForInt() {
        attr.getIntGetter().applyAsInt(999999999999999999d);
    }

    @Test
    public void shouldGetLong() {
        assertEquals(123L, attr.getLongGetter().applyAsLong(123d));
    }

    @Test(expected = ArithmeticException.class)
    public void shouldThrowIfTooBigForLong() {
        attr.getLongGetter().applyAsLong(99999999999999999999d);
    }

    @Test
    public void shouldGetDouble() {
        assertEquals(123d, attr.getDoubleGetter().applyAsDouble(123d), 0.00001d);
    }

    @Test
    public void shouldGetObject() {
        assertEquals(123d, attr.getObjectGetter().apply(123d));
    }

}