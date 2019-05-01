package com.ag.grid.enterprise.oracle.demo;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * @author Yuriy Kiselev (uze@yandex.ru)
 * @since 20.04.2019
 */
public final class ItemMapFactory implements Supplier<Map<String, Object>> {

    private final Map<String, Integer> index = new HashMap<>();

    private int counter = 0;

    private final int size;

    public ItemMapFactory(int size) {
        this.size = size;
    }

    @Override
    public Map<String, Object> get() {
        return new LiteMap();
    }

    private final class LiteMap extends AbstractMap<String, Object> {

        private Object[] values = new Object[size];

        @Override
        public Set<Entry<String, Object>> entrySet() {
            return new AbstractSet<Entry<String, Object>>() {
                @Override
                public Iterator<Entry<String, Object>> iterator() {
                    return new Iterator<Entry<String, Object>>() {

                        final Iterator<Entry<String, Integer>> it = index.entrySet().iterator();

                        @Override
                        public boolean hasNext() {
                            return it.hasNext();
                        }

                        @Override
                        public Entry<String, Object> next() {
                            final Entry<String, Integer> e = it.next();
                            final Integer idx = e.getValue();
                            Object value = null;
                            if (idx != null) {
                                final int i = idx;
                                if (i >= 0 && i <= values.length) {
                                    value = values[i];
                                }
                            }
                            return new SimpleImmutableEntry<>(e.getKey(), value);
                        }
                    };
                }

                @Override
                public int size() {
                    return index.size();
                }
            };
        }

        @Override
        public Object get(Object key) {
            final Integer idx = index.get(key);
            if (idx != null) {
                final int i = idx;
                if (i < values.length) {
                    return values[i];
                }
            }
            return null;
        }

        @Override
        public Object put(String key, Object value) {
            final int idx = index.computeIfAbsent(key, k -> ++counter);
            if (values.length <= idx) {
                values = Arrays.copyOf(values, values.length + 4);
            }
            final Object result = values[idx];
            values[idx] = value;
            return result;
        }
    }
}

