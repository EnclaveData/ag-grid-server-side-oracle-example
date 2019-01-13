package com.ag.grid.enterprise.oracle.demo.dao;

import com.ag.grid.enterprise.oracle.demo.domain.Portfolio;
import com.ag.grid.enterprise.oracle.demo.domain.Trade;
import com.github.ykiselev.ag.grid.api.filter.ColumnFilter;
import com.github.ykiselev.ag.grid.api.filter.GroupKey;
import com.github.ykiselev.ag.grid.data.RequestFilters;
import com.github.ykiselev.ag.grid.data.types.TypeInfo;
import com.google.common.collect.Iterables;
import com.tangosol.net.NamedCache;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.util.Objects.requireNonNull;

/**
 * @author Yuriy Kiselev (uze@yandex.ru)
 * @since 13.01.2019
 */
public final class DefaultPortfolioKeyCache implements PortfolioKeyCache {

    private final Map<Key, ColumnIndex<Long, Portfolio>> indices = new ConcurrentHashMap<>();

    private final int threshold = 25_000;

    private final ExecutorService executorService;

    public DefaultPortfolioKeyCache(ExecutorService executorService) {
        this.executorService = requireNonNull(executorService);
    }

    @Override
    public Collection<Long> getKeys(Portfolio portfolio, RequestFilters filters, TypeInfo<Trade> typeInfo, NamedCache<Long, Trade> trades) {
        if (portfolio.getTradeKeys().size() < threshold) {
            return portfolio.getTradeKeys();
        }
        Collection<Long> smallest = null;
        for (String name : filters.getNames()) {
            final ColumnFilter filter = filters.getFilter(name);
            if (!isEligibleForIndexing(name, filter)) {
                continue;
            }
            final ColumnIndex<Long, Portfolio> index = indices.computeIfAbsent(
                    new Key(portfolio.getName(), name),
                    k -> new DeferredColumnIndex<>(k.getColumn(), Portfolio::getTradeKeys,
                            executorService.submit(() -> indexColumn(portfolio, k.getColumn(),
                                    typeInfo.getAttribute(k.getColumn()).getObjectGetter(), trades)))
            );
            final Collection<Long> keys = index.getKeys(portfolio, filter);
            if (smallest == null || smallest.size() > keys.size()) {
                smallest = keys;
            }
        }
        return Optional.ofNullable(smallest)
                .orElseGet(portfolio::getTradeKeys);
    }

    private ColumnIndex<Long, Portfolio> indexColumn(Portfolio portfolio, String column, Function<Trade, ?> classify, NamedCache<Long, Trade> trades) {
        return new MapBasedColumnIndex<>(
                column,
                StreamSupport.stream(Iterables.partition(portfolio.getTradeKeys(), 1_000).spliterator(), false)
                        .map(trades::getAll)
                        .map(Map::entrySet)
                        .flatMap(Collection::stream)
                        .collect(Collectors.groupingBy(
                                e -> classify.apply(e.getValue()),
                                Collectors.mapping(Map.Entry::getKey, Collectors.toList())
                        ))
        );
    }

    private boolean isEligibleForIndexing(String name, ColumnFilter filter) {
        if (filter instanceof GroupKey) {
            return true;
        }
        return false;
    }

    @Override
    public String getInfo() {
        return indices.entrySet()
                .stream()
                .map(e -> e.getKey() + " : " + e.getValue()).collect(Collectors.joining(",\n"));
    }

    private static final class Key {

        private final String portfolio;

        private final String column;

        Key(String portfolio, String column) {
            this.portfolio = portfolio;
            this.column = column;
        }

        public String getPortfolio() {
            return portfolio;
        }

        public String getColumn() {
            return column;
        }

        @Override
        public String toString() {
            return "Key{" +
                    "portfolio='" + portfolio + '\'' +
                    ", column='" + column + '\'' +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Key key = (Key) o;
            return Objects.equals(portfolio, key.portfolio) &&
                    Objects.equals(column, key.column);
        }

        @Override
        public int hashCode() {
            return Objects.hash(portfolio, column);
        }
    }
}
