package com.github.ykiselev.ag.grid.data.types;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public class IntAttributeTest {

    private final Attribute<Object> attr = new IntAttribute<>("x", v -> (int) v);

    @Test
    public void shouldReturnName() {
        assertEquals("x", attr.getName());
    }

    @Test
    public void shouldReturnType() {
        assertEquals(int.class, attr.getType());
    }

    @Test
    public void shouldGetInt() {
        assertEquals(123, attr.getIntGetter().applyAsInt(123));
    }

    @Test
    public void shouldGetLong() {
        assertEquals(123L, attr.getLongGetter().applyAsLong(123));
    }

    @Test
    public void shouldGetDouble() {
        assertEquals(123d, attr.getDoubleGetter().applyAsDouble(123), 0.00001d);
    }

    @Test
    public void shouldGetObject() {
        assertEquals(123, attr.getObjectGetter().apply(123));
    }
}