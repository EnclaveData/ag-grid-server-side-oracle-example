package com.ag.grid.enterprise.oracle.demo.dao;

import com.ag.grid.enterprise.oracle.demo.domain.Trade;
import com.github.ykiselev.aggrid.domain.filter.ColumnFilter;
import com.github.ykiselev.aggrid.domain.request.AgGridGetRowsRequest;
import com.github.ykiselev.aggrid.domain.response.AgGridGetRowsResponse;
import com.github.ykiselev.aggrid.sources.AgGridRowSource;
import com.github.ykiselev.aggrid.sources.RequestFilters;
import com.github.ykiselev.aggrid.sources.objects.FilteredObjectSource;
import com.github.ykiselev.aggrid.sources.objects.ObjectSource;
import com.github.ykiselev.aggrid.sources.objects.ObjectSourceBasedAgGridRowSource;
import com.github.ykiselev.aggrid.sources.objects.Predicates;
import com.github.ykiselev.aggrid.sources.objects.types.ReflectedTypeInfo;
import com.github.ykiselev.aggrid.sources.objects.types.TypeInfo;
import com.google.common.collect.ImmutableSet;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Repository("inMemoryObjTradeDao")
public class InMemoryObjTradeDao implements TradeDao {

    private final TypeInfo<Trade> typeInfo = ReflectedTypeInfo.of(Trade.class);

    private final Map<Long, Trade> cache = new ConcurrentHashMap<>();

    private final AgGridRowSource rowSource = new ObjectSourceBasedAgGridRowSource<>(
            new ObjectSource<Long, Trade>() {
                @Override
                public FilteredObjectSource<Long, Trade> filter(RequestFilters filters) {
                    final Predicate<Long> keyPredicate;
                    final ColumnFilter keyFilter = filters.getColumnFilter("tradeId");
                    if (keyFilter == null) {
                        keyPredicate = k -> true;
                    } else {
                        // todo
                        keyPredicate = k -> true;
                    }
                    // todo
                    final Predicate<Trade> valuePredicate = v -> true;
                    return new FilteredObjectSource<Long, Trade>() {
                        @Override
                        public Set<String> getFilteredNames() {
                            return Collections.emptySet();
                        }

                        @Override
                        public Iterable<Long> getKeys() {
                            return () ->
                                    cache.keySet()
                                            .stream()
                                            .filter(keyPredicate)
                                            .iterator();
                        }

                        @Override
                        public List<Trade> getAll(Collection<Long> keys) {
                            return keys.stream()
                                    .map(cache::get)
                                    .filter(Objects::nonNull)
                                    .filter(valuePredicate)
                                    .collect(Collectors.toList());
                        }
                    };
                }

                @Override
                public TypeInfo<Trade> getTypeInfo() {
                    return typeInfo;
                }
            }
    );

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
}