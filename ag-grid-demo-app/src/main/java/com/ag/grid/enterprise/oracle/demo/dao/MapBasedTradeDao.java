package com.ag.grid.enterprise.oracle.demo.dao;

import com.ag.grid.enterprise.TradeDumpLoader;
import com.ag.grid.enterprise.oracle.demo.ItemMapFactory;
import com.ag.grid.enterprise.oracle.demo.domain.Trade;
import com.ag.grid.enterprise.oracle.demo.domain.TradeTypeInfoFactory;
import com.github.ykiselev.ag.grid.api.request.AgGridGetRowsRequest;
import com.github.ykiselev.ag.grid.api.request.SortModel;
import com.github.ykiselev.ag.grid.api.request.Sorting;
import com.github.ykiselev.ag.grid.api.response.AgGridGetRowsResponse;
import com.github.ykiselev.ag.grid.data.AgGridRowSource;
import com.github.ykiselev.ag.grid.data.FilteredObjectSource;
import com.github.ykiselev.ag.grid.data.ListBasedAgGridRowSource;
import com.github.ykiselev.ag.grid.data.RequestFilters;
import com.github.ykiselev.ag.grid.data.common.MapUtils;
import com.github.ykiselev.ag.grid.data.types.DefaultTypeInfo;
import com.github.ykiselev.ag.grid.data.types.DoubleAttribute;
import com.github.ykiselev.ag.grid.data.types.LongAttribute;
import com.github.ykiselev.ag.grid.data.types.ObjectAttribute;
import com.github.ykiselev.ag.grid.data.types.ReflectedTypeInfo;
import com.github.ykiselev.ag.grid.data.types.TupleAttribute;
import com.github.ykiselev.ag.grid.data.types.TypeInfo;
import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsFirst;
import static java.util.Objects.requireNonNull;

@Repository("mapBasedTradeDao")
public class MapBasedTradeDao implements TradeDao, AutoCloseable {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final List<Object[]> trades = new ArrayList<>();

    private final List<Trade> originalTrades = new ArrayList<>();

    private final TypeInfo<Object[]> tupleTypeInfo;

    private final TypeInfo<Trade> typeInfo;

    private final AgGridRowSource rowSource;

    public MapBasedTradeDao() {
        typeInfo = //ReflectedTypeInfo.of(Trade.class);
                TradeTypeInfoFactory.create();
/*
        new DefaultTypeInfo<>(Arrays.asList(
                new ObjectAttribute<>("product", String.class, Trade::getProduct),
                new ObjectAttribute<>("portfolio", String.class, Trade::getPortfolio),
                new ObjectAttribute<>("book", String.class, Trade::getBook),
                new LongAttribute<>("tradeId", Trade::getTradeId),
                new LongAttribute<>("submitterId", Trade::getSubmitterId),
                new LongAttribute<>("submitterDealId", Trade::getSubmitterDealId),
                new ObjectAttribute<>("dealType", String.class, Trade::getDealType),
                new ObjectAttribute<>("bidType", String.class, Trade::getBidType),
                new DoubleAttribute<>("currentValue", Trade::getCurrentValue),
                new DoubleAttribute<>("previousValue", Trade::getPreviousValue),
                new DoubleAttribute<>("pl1", Trade::getPl1),
                new DoubleAttribute<>("pl2", Trade::getPl2),
                new DoubleAttribute<>("gainDx", Trade::getGainDx),
                new DoubleAttribute<>("sxPx", Trade::getSxPx),
                new DoubleAttribute<>("x99Out", Trade::getX99Out),
                new LongAttribute<>("batch", Trade::getBatch)
        ));*/

        this.rowSource = new ListBasedAgGridRowSource<>(originalTrades, typeInfo);

        tupleTypeInfo = new DefaultTypeInfo<>(Arrays.asList(
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
                    originalTrades.add(trade);
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

        List<Comparator<Trade>> comparators = Arrays.asList(
                nullsFirst(Comparator.comparing(Trade::getPortfolio, nullsFirst(naturalOrder()))),
                nullsFirst(Comparator.comparing(Trade::getProduct, nullsFirst(naturalOrder()))),
                nullsFirst(Comparator.comparing(Trade::getBook, nullsFirst(naturalOrder()))),
                nullsFirst(Comparator.comparingLong(Trade::getTradeId)),
                nullsFirst(Comparator.comparing(Trade::getDealType, nullsFirst(naturalOrder()))),
                nullsFirst(Comparator.comparing(Trade::getBidType, nullsFirst(naturalOrder()))),
                nullsFirst(Comparator.comparingDouble(Trade::getCurrentValue)),
                nullsFirst(Comparator.comparingDouble(Trade::getPreviousValue))
        );
        for (Comparator<Trade> comparator : comparators) {
            System.out.println("Using " + comparator);
            for (int i = 0; i < 0; i++) {
                Stopwatch sw = Stopwatch.createStarted();
                final Object[] a = originalTrades.toArray();
                Arrays.parallelSort(a, (Comparator) comparator);
                System.out.println(i + ") Original trade list sorted in " + sw);
            }
        }
/*
        final Comparator<Object[]> tupleComparator = comparator("book", Sorting.ASC);
        final Comparator<Object[]> tupleComparator2 = comparator("currentValue", Sorting.ASC);
        for (int i = 0; i < 10; i++) {
            Stopwatch sw = Stopwatch.createStarted();
            trades.sort(tupleComparator2);
            System.out.println(i + ") List of tuples sorted in " + sw);
        }*/
    }

    private Comparator<Object[]> comparator(String name, Sorting sorting) {
        Comparator<Object[]> comparator = tupleTypeInfo.getAttribute(name).getComparator();
        if (Sorting.ASC != sorting) {
            comparator = comparator.reversed();
        }
        return nullsFirst(comparator);
    }

    private Comparator<Map<String, Object>> mapComparator(SortModel sortModel) {
        Comparator<Map<String, Object>> comparator = Comparator.comparing(
                MapUtils.extractValue(sortModel.getColId()),
                nullsFirst(naturalOrder())
        );
        if (Sorting.ASC != sortModel.getSort()) {
            comparator = comparator.reversed();
        }
        return nullsFirst(comparator);
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