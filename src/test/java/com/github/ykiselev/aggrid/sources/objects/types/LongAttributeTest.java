package com.github.ykiselev.aggrid.sources.objects.types;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public class LongAttributeTest {

    private final Attribute<Object> attr = new LongAttribute<>("y", v -> (long) v);

    @Test
    public void shouldReturnName() {
        assertEquals("y", attr.getName());
    }

    @Test
    public void shouldReturnType() {
        assertEquals(long.class, attr.getType());
    }

    @Test
    public void shouldGetInt() {
        assertEquals(123, attr.getIntGetter().applyAsInt(123L));
    }

    @Test(expected = ArithmeticException.class)
    public void shouldThrowIfTooBigForInt() {
        attr.getIntGetter().applyAsInt(999999999999999999L);
    }

    @Test
    public void shouldGetLong() {
        assertEquals(123L, attr.getLongGetter().applyAsLong(123L));
    }

    @Test
    public void shouldGetDouble() {
        assertEquals(123d, attr.getDoubleGetter().applyAsDouble(123L), 0.00001d);
    }

    @Test
    public void shouldGetObject() {
        assertEquals(123L, attr.getObjectGetter().apply(123L));
    }
}