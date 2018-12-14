package com.ag.grid.enterprise.oracle.demo.data.types;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public class ReflectedTypeInfoTest {

    @Test
    public void shouldCreateTypeInfo() {
        TypeInfo<MyBean> typeInfo = ReflectedTypeInfo.of(MyBean.class);
        MyBean bean = new MyBean(1L, "name", 2d, 3d, 4f, (short) 5);
        Map<String, Attribute<MyBean>> attrs = typeInfo.getAttributes();

        assertEquals(1L, attrs.get("id").getLongGetter().applyAsLong(bean));
        assertEquals("name", attrs.get("name").getObjectGetter().apply(bean));
        assertEquals(2d, attrs.get("amount").getDoubleGetter().applyAsDouble(bean), 0.0001d);
        assertEquals(3d, attrs.get("sum").getDoubleGetter().applyAsDouble(bean), 0.0001d);
        assertEquals(3d, attrs.get("sum").getObjectGetter().apply(bean));
        assertEquals(4f, attrs.get("x").getDoubleGetter().applyAsDouble(bean), 0.0001f);
        assertEquals((short) 5, attrs.get("y").getIntGetter().applyAsInt(bean));
    }

    public static class MyBean {

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

        public MyBean(long id, String name, double amount, Double sum, float x, short y) {
            this.id = id;
            this.name = name;
            this.amount = amount;
            this.sum = sum;
            this.x = x;
            this.y = y;
        }
    }
}