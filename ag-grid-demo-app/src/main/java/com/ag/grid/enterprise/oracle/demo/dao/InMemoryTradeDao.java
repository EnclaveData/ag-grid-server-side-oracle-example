package com.ag.grid.enterprise.oracle.demo.dao;

import com.ag.grid.enterprise.TradeDumpLoader;
import com.ag.grid.enterprise.oracle.demo.domain.Trade;
import com.ag.grid.enterprise.oracle.demo.domain.TradeTypeInfoFactory;
import com.github.ykiselev.ag.grid.api.request.AgGridGetRowsRequest;
import com.github.ykiselev.ag.grid.api.response.AgGridGetRowsResponse;
import com.github.ykiselev.ag.grid.data.AgGridRowSource;
import com.github.ykiselev.ag.grid.data.ListBasedAgGridRowSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository("inMemoryTradeDao")
@Lazy
public class InMemoryTradeDao implements TradeDao, AutoCloseable {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final List<Trade> originalTrades = new ArrayList<>();

    private final AgGridRowSource rowSource;

    public InMemoryTradeDao() {
        this.rowSource = new ListBasedAgGridRowSource<>(
                originalTrades,
                TradeTypeInfoFactory.create()
        );
    }

    @PostConstruct
    private void init() {
        logger.info("Loading data...");
        final Map<String, Map<Long, Trade>> map = TradeDumpLoader.load();

        logger.info("Putting trades...");
        map.values().stream()
                .flatMap(m -> m.entrySet().stream())
                .forEach(e -> {
                    final Trade trade = e.getValue();
                    originalTrades.add(trade);
                });
        logger.info("Done!");
    }

    @Override
    public void close() {
    }

    @Override
    public AgGridGetRowsResponse getData(AgGridGetRowsRequest request) {
        return rowSource.getRows(request);
    }
}