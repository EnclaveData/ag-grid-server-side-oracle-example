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
import com.github.ykiselev.aggrid.sources.objects.types.Attribute;
import com.github.ykiselev.aggrid.sources.objects.types.ReflectedTypeInfo;
import com.github.ykiselev.aggrid.sources.objects.types.TypeInfo;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;

@Repository("inMemoryObjTradeDao")
public class InMemoryObjTradeDao implements TradeDao {

    private final TypeInfo<Trade> typeInfo = ReflectedTypeInfo.of(Trade.class);

    private static final class StringAttribute implements Attribute<String> {

        @Override
        public String getName() {
            return "portfolio";
        }

        @Override
        public Class<?> getType() {
            return String.class;
        }

        @Override
        public ToIntFunction<String> getIntGetter() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ToLongFunction<String> getLongGetter() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ToDoubleFunction<String> getDoubleGetter() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Function<String, ?> getObjectGetter() {
            return Function.identity();
        }
    }

    private final Attribute<String> stringAttribute = new StringAttribute();

    private final AtomicReference<Map<String, Map<Long, Trade>>> tradesByPortfolio = new AtomicReference<>(Collections.emptyMap());

    private final AtomicReference<Map<Long, Trade>> allTrades = new AtomicReference<>(Collections.emptyMap());

    private final AgGridRowSource rowSource = new ObjectSourceBasedAgGridRowSource<>(
            new ObjectSource<Long, Trade>() {
                @Override
                public FilteredObjectSource<Long, Trade> filter(RequestFilters filters) {
                    final ColumnFilter keyFilter = filters.getColumnFilter("portfolio");
                    final Predicate<String> keyPredicate;
                    if (keyFilter != null) {
                        keyPredicate = Predicates.predicate(stringAttribute, keyFilter);
                    } else {
                        keyPredicate = null;
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
                            return () -> {
                                if (keyPredicate != null) {
                                    final List<String> portfolios = tradesByPortfolio.get()
                                            .keySet()
                                            .stream()
                                            .filter(keyPredicate)
                                            .collect(Collectors.toList());
                                    return portfolios.stream()
                                            .map(tradesByPortfolio.get()::get)
                                            .filter(Objects::nonNull)
                                            .flatMap(m -> m.keySet().stream())
                                            .iterator();
                                } else {
                                    return allTrades.get()
                                            .keySet()
                                            .stream()
                                            .iterator();
                                }
                            };
                        }

                        @Override
                        public List<Trade> getAll(Collection<Long> keys) {
                            final Map<Long, Trade> map = allTrades.get();
                            return keys.stream()
                                    .map(map::get)
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
            },
            5_000
    );

    @PostConstruct
    private void init() throws IOException, ClassNotFoundException {
        final Map<String, Map<Long, Trade>> map;
        try (InputStream is = Files.newInputStream(Paths.get(System.getProperty("tradesDumpFile", "trades.jser")));
             InputStream bis = new BufferedInputStream(is);
             ObjectInputStream ois = new ObjectInputStream(bis)
        ) {
            map = (Map<String, Map<Long, Trade>>) ois.readObject();
        }
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