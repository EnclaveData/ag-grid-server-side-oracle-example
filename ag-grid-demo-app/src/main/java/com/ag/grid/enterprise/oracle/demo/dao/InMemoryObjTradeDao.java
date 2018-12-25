package com.ag.grid.enterprise.oracle.demo.dao;

import com.ag.grid.enterprise.TradeDumpLoader;
import com.ag.grid.enterprise.oracle.demo.domain.Trade;
import com.github.ykiselev.ag.grid.api.filter.ColumnFilter;
import com.github.ykiselev.ag.grid.api.request.AgGridGetRowsRequest;
import com.github.ykiselev.ag.grid.api.response.AgGridGetRowsResponse;
import com.github.ykiselev.ag.grid.data.AgGridRowSource;
import com.github.ykiselev.ag.grid.data.ObjectSource;
import com.github.ykiselev.ag.grid.data.ObjectSourceBasedAgGridRowSource;
import com.github.ykiselev.ag.grid.data.RequestFilters;
import com.github.ykiselev.ag.grid.data.common.Predicates;
import com.github.ykiselev.ag.grid.data.types.Attribute;
import com.github.ykiselev.ag.grid.data.types.ObjectAttribute;
import com.github.ykiselev.ag.grid.data.types.ReflectedTypeInfo;
import com.github.ykiselev.ag.grid.data.types.TypeInfo;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository("inMemoryObjTradeDao")
public class InMemoryObjTradeDao implements TradeDao {

    private final TypeInfo<Trade> typeInfo = ReflectedTypeInfo.of(Trade.class);

    private final Attribute<String> stringAttribute = new ObjectAttribute<>("portfolio", String.class, Function.identity());

    private final AtomicReference<Map<String, Map<Long, Trade>>> tradesByPortfolio = new AtomicReference<>(Collections.emptyMap());

    private final AtomicReference<Map<Long, Trade>> allTrades = new AtomicReference<>(Collections.emptyMap());

    private final AgGridRowSource rowSource = new ObjectSourceBasedAgGridRowSource<>(
            new ObjectSource<Trade>() {
                @Override
                public Set<String> getFilteredNames() {
                    return Collections.emptySet();
                }

                @Override
                public Stream<Trade> getAll(RequestFilters filters) {
                    final ColumnFilter keyFilter = filters.getColumnFilter("portfolio");
                    if (keyFilter != null) {
                        return tradesByPortfolio.get()
                                .keySet()
                                .stream()
                                .filter(Predicates.predicate(stringAttribute, keyFilter))
                                .map(tradesByPortfolio.get()::get)
                                .filter(Objects::nonNull)
                                .flatMap(m -> m.values().stream());
                    } else {
                        return allTrades.get()
                                .values()
                                .stream();
                    }
                }

                @Override
                public TypeInfo<Trade> getTypeInfo() {
                    return typeInfo;
                }
            }
    );

    @PostConstruct
    private void init() throws IOException, ClassNotFoundException {
        final Map<String, Map<Long, Trade>> map = TradeDumpLoader.load();
        tradesByPortfolio.set(map);
        allTrades.set(
                map.values()
                        .stream()
                        .flatMap(m -> m.values().stream())
                        .collect(Collectors.toMap(
                                Trade::getTradeId,
                                Function.identity()
                        ))
        );
    }

    @Override
    public AgGridGetRowsResponse getData(AgGridGetRowsRequest request) {
        return rowSource.getRows(request);
    }
}