package com.ag.grid.enterprise.oracle.demo.dao;

import com.ag.grid.enterprise.oracle.demo.builder.CohQueryBuilder;
import com.ag.grid.enterprise.oracle.demo.domain.Trade;
import com.github.ykiselev.aggrid.domain.request.AgGridGetRowsRequest;
import com.github.ykiselev.aggrid.domain.response.AgGridGetRowsResponse;
import com.oracle.common.util.Duration;
import com.tangosol.coherence.dslquery.CoherenceQueryLanguage;
import com.tangosol.coherence.dslquery.ExecutionContext;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.extractor.ReflectionExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

//@Repository("cohQlBasedTradeDao")
public class CohQlBasedTradeDao implements TradeDao {

    private static final int PIVOT_VALUES_GLOBAL_LIMIT = 100;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final NamedCache<Long, Trade> cache;

    private final CohQueryBuilder queryBuilder;

    private final ExecutionContext context;

    private final StringWriter output = new LimitedStringWriter();

    public CohQlBasedTradeDao() {
        this.cache = CacheFactory.getCache("Trades");
        this.queryBuilder = new CohQueryBuilder();
        this.context = new ExecutionContext();
    }

    @PostConstruct
    private void init() {
        context.setTimeout(new Duration(30, Duration.Magnitude.SECOND));
        context.setTraceEnabled(false);
        context.setSanityCheckingEnabled(false);
        context.setExtendedLanguage(true);
        context.setWriter(new PrintWriter(output));
        context.setCoherenceQueryLanguage(new CoherenceQueryLanguage());

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
        // todo
        final List<Map<String, Object>> rows = Collections.emptyList();
        final int currentLastRow = request.getStartRow() + rows.size();
        final int lastRow = currentLastRow <= request.getEndRow() ? currentLastRow : -1;
        return new AgGridGetRowsResponse(rows, lastRow, new ArrayList<>());
    }

    private void query(String query) {
        logger.info("Query: {}", query);
        final StringBuffer sb = output.getBuffer();
        sb.setLength(0);
        final Object result = context.getStatementExecutor().execute(new StringReader(query), context);
        if (result == null) {
            logger.error(sb.toString());
        } else if (result instanceof Map) {
            int k = 0;
        } else if (result instanceof Set) {
            int k = 1;
        }
    }
}