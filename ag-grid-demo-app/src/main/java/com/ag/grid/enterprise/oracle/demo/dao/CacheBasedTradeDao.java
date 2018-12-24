package com.ag.grid.enterprise.oracle.demo.dao;

import com.ag.grid.enterprise.TradeDumpLoader;
import com.ag.grid.enterprise.oracle.demo.builder.CacheQueryBuilder;
import com.ag.grid.enterprise.oracle.demo.builder.CohFilters;
import com.ag.grid.enterprise.oracle.demo.domain.Portfolio;
import com.ag.grid.enterprise.oracle.demo.domain.Trade;
import com.github.ykiselev.ag.grid.api.filter.ColumnFilter;
import com.github.ykiselev.ag.grid.api.filter.GroupKey;
import com.github.ykiselev.ag.grid.api.request.AgGridGetRowsRequest;
import com.github.ykiselev.ag.grid.api.response.AgGridGetRowsResponse;
import com.github.ykiselev.ag.grid.data.AgGridRowSource;
import com.github.ykiselev.ag.grid.data.ObjectSource;
import com.github.ykiselev.ag.grid.data.ObjectSourceBasedAgGridRowSource;
import com.github.ykiselev.ag.grid.data.RequestFilters;
import com.github.ykiselev.ag.grid.data.types.ReflectedTypeInfo;
import com.github.ykiselev.ag.grid.data.types.TypeInfo;
import com.google.common.collect.Iterables;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.Filter;
import com.tangosol.util.extractor.KeyExtractor;
import com.tangosol.util.extractor.ReflectionExtractor;
import com.tangosol.util.filter.AndFilter;
import com.tangosol.util.filter.EqualsFilter;
import com.tangosol.util.filter.InFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Repository("cacheBasedTradeDao")
public class CacheBasedTradeDao implements TradeDao {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final NamedCache<Long, Trade> trades;

    private final NamedCache<String, Portfolio> portfolios;

    private final TypeInfo<Trade> typeInfo = ReflectedTypeInfo.of(Trade.class);

    private final AgGridRowSource rowSource = new ObjectSourceBasedAgGridRowSource<>(
            new ObjectSource<Trade>() {
                @Override
                public Set<String> getFilteredNames() {
                    return Collections.emptySet();
                }

                @Override
                public Stream<Trade> getAll(RequestFilters filters) {
                    ColumnFilter keyFilter = filters.getColumnFilter("portfolio");
                    if (keyFilter != null && filters.getNames().size() < 2){
                        final List<Long> keys = portfolios.entrySet(CohFilters.toFilter(keyFilter, new KeyExtractor()))
                                .stream()
                                .map(Map.Entry::getValue)
                                .map(Portfolio::getTradeKeys)
                                .flatMap(Collection::stream)
                                .collect(Collectors.toList());

                        return StreamSupport.stream(Iterables.partition(keys, 150).spliterator(), false)
                                .flatMap(k -> trades.getAll(k).values().stream());
                    }
                    logger.info("Falling back to filtering in cache...");
                    Filter filter = CohFilters.filter(filters);
                    return trades.values(filter).stream();
                }

                @Override
                public TypeInfo<Trade> getTypeInfo() {
                    return typeInfo;
                }
            }
    );

    public CacheBasedTradeDao() {
        this.trades = CacheFactory.getCache("Trades");
        this.portfolios = CacheFactory.getCache("Portfolios");
    }

    @PostConstruct
    private void init() {
        trades.addIndex(new ReflectionExtractor<>("getProduct"), false, null);
        trades.addIndex(new ReflectionExtractor<>("getPortfolio"), false, null);
        trades.addIndex(new ReflectionExtractor<>("getBook"), false, null);

        if (trades.isEmpty()) {
            logger.info("Loading data...");
            final Map<String, Map<Long, Trade>> map = TradeDumpLoader.load();

            logger.info("Putting trades...");
            for (Map<Long, Trade> tradeMap : map.values()) {
                trades.putAll(tradeMap);
            }

            logger.info("Putting portfolios...");
            portfolios.putAll(
                    map.entrySet()
                            .stream()
                            .map(e -> new Portfolio(e.getKey(), new HashSet<>(e.getValue().keySet())))
                            .collect(Collectors.toMap(
                                    Portfolio::getName,
                                    Function.identity()
                            ))
            );
            logger.info("Data loaded.");
        }
    }

    @Override
    public AgGridGetRowsResponse getData(AgGridGetRowsRequest request) {
        return rowSource.getRows(request);/*
        final CacheQueryBuilder builder = new CacheQueryBuilder(request);
        final Filter filter = builder.filter();
        Object result;
        if (builder.isGrouping()) {
            result = trades.aggregate(filter, builder.groupAggregator());
        } else {
            result = trades.entrySet(filter);
        }
        final List<?> rows = builder.parseResult(result);
        final int currentLastRow = request.getStartRow() + rows.size();
        final int lastRow = currentLastRow <= request.getEndRow() ? currentLastRow : -1;
        return new AgGridGetRowsResponse<>(rows, lastRow, new ArrayList<>(builder.getSecondaryColumns()));*/
    }
}