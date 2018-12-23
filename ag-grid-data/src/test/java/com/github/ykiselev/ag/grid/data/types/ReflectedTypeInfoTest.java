package com.github.ykiselev.ag.grid.data.types;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public class ReflectedTypeInfoTest {

    @Test
    public void shouldCreateTypeInfo() {
        TypeInfo<MyBean> typeInfo = ReflectedTypeInfo.of(MyBean.class);
        MyBean bean = new MyBean(1L, "name", 2d, 3d, 4f, (short) 5);

        assertEquals(1L, typeInfo.getAttribute("id").getLongGetter().applyAsLong(bean));
        assertEquals("name", typeInfo.getAttribute("name").getObjectGetter().apply(bean));
        assertEquals(2d, typeInfo.getAttribute("amount").getDoubleGetter().applyAsDouble(bean), 0.0001d);
        assertEquals(3d, typeInfo.getAttribute("sum").getDoubleGetter().applyAsDouble(bean), 0.0001d);
        assertEquals(3d, typeInfo.getAttribute("sum").getObjectGetter().apply(bean));
        assertEquals(4f, typeInfo.getAttribute("x").getDoubleGetter().applyAsDouble(bean), 0.0001f);
        assertEquals((short) 5, typeInfo.getAttribute("y").getIntGetter().applyAsInt(bean));
        assertFalse(typeInfo.hasName("class"));
    }

    static class MyBean {

        private final long id;

        private final String name;

        private final double amount;

        private final Double sum;

        private final float x;

        private final short y;

        public long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public double getAmount() {
            return amount;
        }

        public Double getSum() {
            return sum;
        }

        public float getX() {
            return x;
        }

        public short getY() {
            return y;
        }

        MyBean(long id, String name, double amount, Double sum, float x, short y) {
            this.id = id;
            this.name = name;
            this.amount = amount;
            this.sum = sum;
            this.x = x;
            this.y = y;
        }
    }
}