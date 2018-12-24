package com.ag.grid.enterprise.oracle.demo.domain;

import java.io.Serializable;
import java.util.Collection;

import static java.util.Objects.requireNonNull;

/**
 * @author Yuriy Kiselev (uze@yandex.ru)
 * @since 23.12.2018
 */
public final class Portfolio implements Serializable {

    private static final long serialVersionUID = 8000236302989267738L;

    private Collection<Long> tradeKeys;

    private String name;

    public Collection<Long> getTradeKeys() {
        return tradeKeys;
    }

    public void setTradeKeys(Collection<Long> tradeKeys) {
        this.tradeKeys = tradeKeys;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Portfolio() {
    }

    public Portfolio(String name, Collection<Long> tradeKeys) {
        this.tradeKeys = requireNonNull(tradeKeys);
        this.name = requireNonNull(name);
    }
}
