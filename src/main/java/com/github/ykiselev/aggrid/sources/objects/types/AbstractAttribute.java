package com.github.ykiselev.aggrid.sources.objects.types;

import static java.util.Objects.requireNonNull;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public abstract class AbstractAttribute<V> implements Attribute<V> {

    private final String name;

    private final Class<?> type;

    protected AbstractAttribute(String name, Class<?> type) {
        this.name = requireNonNull(name);
        this.type = requireNonNull(type);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Class<?> getType() {
        return type;
    }
}
