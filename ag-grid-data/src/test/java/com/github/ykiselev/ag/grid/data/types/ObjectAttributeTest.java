package com.github.ykiselev.ag.grid.data.types;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public class ObjectAttributeTest {

    private final Attribute<Object> attr = new ObjectAttribute<>("obj", String.class, v -> v);

    @Test
    public void shouldReturnName() {
        assertEquals("obj", attr.getName());
    }

    @Test
    public void shouldReturnType() {
        assertEquals(String.class, attr.getType());
    }

    @Test
    public void shouldGetInt() {
        assertEquals(1, attr.getIntGetter().applyAsInt(1));
    }

    @Test
    public void shouldGetLong() {
        assertEquals(1L, attr.getLongGetter().applyAsLong(1L));
    }

    @Test
    public void shouldGetDouble() {
        assertEquals(Math.PI, attr.getDoubleGetter().applyAsDouble(Math.PI), 0.00001d);
    }

    @Test
    public void shouldGetObject() {
        assertEquals("abc", attr.getObjectGetter().apply("abc"));
    }

}