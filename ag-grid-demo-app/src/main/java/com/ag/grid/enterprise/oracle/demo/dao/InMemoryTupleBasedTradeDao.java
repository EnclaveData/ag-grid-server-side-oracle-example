package com.ag.grid.enterprise.oracle.demo.dao;

import com.ag.grid.enterprise.TradeDumpLoader;
import com.ag.grid.enterprise.oracle.demo.ItemMapFactory;
import com.ag.grid.enterprise.oracle.demo.domain.Trade;
import com.ag.grid.enterprise.oracle.demo.domain.TradeTypeInfoFactory;
import com.github.ykiselev.ag.grid.api.request.AgGridGetRowsRequest;
import com.github.ykiselev.ag.grid.api.response.AgGridGetRowsResponse;
import com.github.ykiselev.ag.grid.data.AgGridRowSource;
import com.github.ykiselev.ag.grid.data.FilteredObjectSource;
import com.github.ykiselev.ag.grid.data.ListBasedAgGridRowSource;
import com.github.ykiselev.ag.grid.data.ObjectSourceBasedAgGridRowSource;
import com.github.ykiselev.ag.grid.data.RequestFilters;
import com.github.ykiselev.ag.grid.data.types.DefaultTypeInfo;
import com.github.ykiselev.ag.grid.data.types.TupleAttribute;
import com.github.ykiselev.ag.grid.data.types.TypeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

@Repository("inMemoryTupleBasedTradeDao")
@Lazy
public class InMemoryTupleBasedTradeDao implements TradeDao, AutoCloseable {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final List<Object[]> trades = new ArrayList<>();

    private final TypeInfo<Object[]> tupleTypeInfo;

    private final AgGridRowSource rowSource;

    public InMemoryTupleBasedTradeDao() {
        this.rowSource = new ObjectSourceBasedAgGridRowSource<>(FilteredTradeSource::new);
        this.tupleTypeInfo = new DefaultTypeInfo<>(Arrays.asList(
                new TupleAttribute("product", String.class, 0),
                new TupleAttribute("portfolio", String.class, 1),
                new TupleAttribute("book", String.class, 2),
                new TupleAttribute("tradeId", long.class, 3),
                new TupleAttribute("submitterId", long.class, 4),
                new TupleAttribute("submitterDealId", long.class, 5),
                new TupleAttribute("dealType", String.class, 6),
                new TupleAttribute("bidType", String.class, 7),
                new TupleAttribute("currentValue", double.class, 8),
                new TupleAttribute("previousValue", double.class, 9),
                new TupleAttribute("pl1", double.class, 10),
                new TupleAttribute("pl2", double.class, 11),
                new TupleAttribute("gainDx", double.class, 12),
                new TupleAttribute("sxPx", double.class, 13),
                new TupleAttribute("x99Out", double.class, 14),
                new TupleAttribute("batch", long.class, 15)
        ), new ItemMapFactory(16));
    }

    @PostConstruct
    private void init() {
        logger.info("Loading data...");
        final Map<String, Map<Long, Trade>> map = TradeDumpLoader.load();

        logger.info("Putting trades...");
        map.values().stream()
                .flatMap(m -> m.entrySet().stream())
                .map(e -> {
                    final Trade trade = e.getValue();
                    final Object[] tuple = new Object[16];
                    int i = 0;
                    tuple[i++] = trade.getProduct();
                    tuple[i++] = trade.getPortfolio();
                    tuple[i++] = trade.getBook();
                    tuple[i++] = trade.getTradeId();
                    tuple[i++] = trade.getSubmitterId();
                    tuple[i++] = trade.getSubmitterDealId();
                    tuple[i++] = trade.getDealType();
                    tuple[i++] = trade.getBidType();
                    tuple[i++] = trade.getCurrentValue();
                    tuple[i++] = trade.getPreviousValue();
                    tuple[i++] = trade.getPl1();
                    tuple[i++] = trade.getPl2();
                    tuple[i++] = trade.getGainDx();
                    tuple[i++] = trade.getSxPx();
                    tuple[i++] = trade.getX99Out();
                    tuple[i++] = trade.getBatch();
                    return tuple;
                }).forEach(trades::add);
        logger.info("Done!");
    }

    @Override
    public void close() {
    }

    @Override
    public AgGridGetRowsResponse getData(AgGridGetRowsRequest request) {
        return rowSource.getRows(request);
    }

    private final class FilteredTradeSource implements FilteredObjectSource<Object[]> {

        private final RequestFilters filters;

        FilteredTradeSource(RequestFilters filters) {
            this.filters = requireNonNull(filters);
        }

        @Override
        public TypeInfo<Object[]> getTypeInfo() {
            return tupleTypeInfo;
        }

        @Override
        public RequestFilters getFilters() {
            return filters;
        }

        @Override
        public Set<String> getFilteredNames() {
            return Collections.emptySet();
        }

        @Override
        public Stream<Object[]> stream() {
            return trades.stream();
        }

        @Override
        public void close() {
        }
    }
}