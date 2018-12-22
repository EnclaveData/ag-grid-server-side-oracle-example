package com.ag.grid.enterprise.oracle.demo.dao;

import com.ag.grid.enterprise.oracle.demo.builder.CacheQueryBuilder;
import com.ag.grid.enterprise.oracle.demo.domain.Trade;
import com.github.ykiselev.ag.grid.api.request.AgGridGetRowsRequest;
import com.github.ykiselev.ag.grid.api.response.AgGridGetRowsResponse;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.Filter;
import com.tangosol.util.extractor.ReflectionExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

//@Repository("cacheBasedTradeDao")
public class CacheBasedTradeDao implements TradeDao {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final NamedCache<Long, Trade> cache;

    public CacheBasedTradeDao() {
        this.cache = CacheFactory.getCache("Trades");
    }

    @PostConstruct
    private void init() {
        cache.addIndex(new ReflectionExtractor<>("getProduct"), true, null);
        //cache.addIndex(new ReflectionExtractor<>("getPortfolio"), true, null);
        //cache.addIndex(new ReflectionExtractor<>("getBook"), true, null);

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
        final CacheQueryBuilder builder = new CacheQueryBuilder(request);
        final Filter filter = builder.filter();
        Object result;
        if (builder.isGrouping()) {
            result = cache.aggregate(filter, builder.groupAggregator());
        } else {
            result = cache.entrySet(filter);
        }
        final List<Map<String, Object>> rows = builder.parseResult(result);
        final int currentLastRow = request.getStartRow() + rows.size();
        final int lastRow = currentLastRow <= request.getEndRow() ? currentLastRow : -1;
        return new AgGridGetRowsResponse(rows, lastRow, new ArrayList<>(builder.getSecondaryColumns()));
    }
}