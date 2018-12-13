package com.ag.grid.enterprise.oracle.demo.dao;

import com.ag.grid.enterprise.oracle.demo.data.AgGridRowSource;
import com.ag.grid.enterprise.oracle.demo.data.RequestFilters;
import com.ag.grid.enterprise.oracle.demo.data.mapsource.FilteredMapSource;
import com.ag.grid.enterprise.oracle.demo.data.mapsource.MapSourceBasedAgGridRowSource;
import com.ag.grid.enterprise.oracle.demo.data.mapsource.MapUtils;
import com.ag.grid.enterprise.oracle.demo.data.types.ReflectedTypeInfo;
import com.ag.grid.enterprise.oracle.demo.data.types.TypeInfo;
import com.ag.grid.enterprise.oracle.demo.domain.Trade;
import com.ag.grid.enterprise.oracle.demo.filter.ColumnFilter;
import com.ag.grid.enterprise.oracle.demo.request.AgGridGetRowsRequest;
import com.ag.grid.enterprise.oracle.demo.response.AgGridGetRowsResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Repository("inMemoryMapTradeDao")
public class InMemoryMapTradeDao implements TradeDao {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<Long, Trade> cache = new ConcurrentHashMap<>();

    private final AgGridRowSource rowSource = new MapSourceBasedAgGridRowSource<>(this::filter);

    private final ObjectMapper mapper = new ObjectMapper();

    private final TypeInfo typeInfo = ReflectedTypeInfo.of(Trade.class);

    @PostConstruct
    private void init() {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        Map<Long, Trade> map = new HashMap<>();
        for (int i = 1; i <= 1_000_000; i++) {
            Trade t = new Trade();
            t.setProduct("product_" + (i % 100));
            t.setPortfolio("portfolio_" + (i % 1500));
            t.setBook("book_" + (i % 5000));
            t.setTradeId(i);
            t.setSubmitterId(i % 3000);
            t.setSubmitterDealId(i);
            t.setDealType("dealType_" + (i % 50));
            t.setBidType("bidType_" + (i % 250));
            t.setCurrentValue(rnd.nextDouble(0, 100_000));
            t.setPreviousValue(rnd.nextDouble(0, 100_000));
            t.setPl1(rnd.nextDouble());
            t.setPl2(rnd.nextDouble());
            t.setGainDx(rnd.nextDouble());
            t.setSxPx(rnd.nextDouble());
            t.setX99Out(rnd.nextDouble());
            t.setBatch(i % 15000);
            map.put(t.getTradeId(), t);
            if (i % 5_000 == 0) {
                cache.putAll(map);
                map.clear();
            }
        }
    }

    @Override
    public AgGridGetRowsResponse getData(AgGridGetRowsRequest request) {
        return rowSource.getRows(request);
    }

    private FilteredMapSource<Long> filter(RequestFilters filters) {
        final Predicate<Long> keyPredicate;
        final ColumnFilter keyFilter = filters.getColumnFilter("tradeId");
        if (keyFilter == null) {
            keyPredicate = k -> true;
        } else {
            // todo
            keyPredicate = k -> true;
        }
        final Predicate<Map<String, Object>> valuePredicate = MapUtils.predicate(filters, typeInfo);
        return new FilteredMapSource<Long>() {
            @Override
            public Iterable<Long> getKeys() {
                return () ->
                        cache.keySet()
                                .stream()
                                .filter(keyPredicate)
                                .iterator();
            }

            @Override
            public List<Map<String, Object>> getAll(Collection<Long> keys) {
                return keys.stream()
                        .map(cache::get)
                        .filter(Objects::nonNull)
                        .map(this::toMap)
                        .filter(valuePredicate)
                        .collect(Collectors.toList());
            }

            private Map<String, Object> toMap(Trade t) {
                return mapper.convertValue(t, new TypeReference<Map<String, Object>>() {
                });
            }
        };
    }
}