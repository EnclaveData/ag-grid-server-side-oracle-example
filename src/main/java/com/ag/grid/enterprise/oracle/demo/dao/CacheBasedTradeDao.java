package com.ag.grid.enterprise.oracle.demo.dao;

import com.ag.grid.enterprise.oracle.demo.builder.CacheQueryBuilder;
import com.ag.grid.enterprise.oracle.demo.builder.CohQueryBuilder;
import com.ag.grid.enterprise.oracle.demo.domain.Trade;
import com.ag.grid.enterprise.oracle.demo.request.ColumnVO;
import com.ag.grid.enterprise.oracle.demo.request.EnterpriseGetRowsRequest;
import com.ag.grid.enterprise.oracle.demo.response.EnterpriseGetRowsResponse;
import com.oracle.common.util.Duration;
import com.tangosol.coherence.dslquery.CoherenceQueryLanguage;
import com.tangosol.coherence.dslquery.ExecutionContext;
import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.Filter;
import com.tangosol.util.ValueExtractor;
import com.tangosol.util.aggregator.DistinctValues;
import com.tangosol.util.aggregator.GroupAggregator;
import com.tangosol.util.extractor.ReflectionExtractor;
import com.tangosol.util.filter.AllFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import static com.ag.grid.enterprise.oracle.demo.builder.EnterpriseResponseBuilder.createResponse;
import static com.google.common.collect.Streams.zip;
import static java.util.stream.Collectors.toMap;

@Repository("cacheBasedTradeDao")
public class CacheBasedTradeDao implements TradeDao {

    private static final int PIVOT_VALUES_GLOBAL_LIMIT = 100;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final NamedCache<Long, Trade> cache;

    //private final CohQueryBuilder queryBuilder;

    private final ExecutionContext context;

    private final StringWriter output = new LimitedStringWriter();

    public CacheBasedTradeDao() {
        this.cache = CacheFactory.getCache("Trades");
        //this.queryBuilder = new CohQueryBuilder();
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
    public EnterpriseGetRowsResponse getData(EnterpriseGetRowsRequest request) {
        CacheQueryBuilder builder = new CacheQueryBuilder(request);
        List<Map<String, Object>> rows = Collections.emptyList();

        if (builder.isGrouping()){
            Object aggregate = cache.aggregate(builder.filter(), builder.groupAggregator());
            rows = builder.parseResult(aggregate);
        }

        // first obtain the pivot values from the DB for the requested pivot columns
        Map<String, List<String>> pivotValues = Collections.emptyMap();
//        request.isPivotMode()
//                ? getPivotValues(request.getPivotCols())
//                : Collections.emptyMap();

        // generate sql
        //String sql = queryBuilder.createSql(request, "Trades", pivotValues);

        // query db for rows
        //List<Map<String, Object>> rows = queryForList(sql);

        // create response with our results
        return createResponse(request, rows, pivotValues);
    }

    private Map<String, List<String>> getPivotValues(List<ColumnVO> pivotCols) {
        final int columnLimit;
        if (pivotCols.isEmpty()) {
            columnLimit = 0;
        } else if (pivotCols.size() == 1) {
            columnLimit = PIVOT_VALUES_GLOBAL_LIMIT;
        } else {
            columnLimit = (int) Math.floor(Math.pow(PIVOT_VALUES_GLOBAL_LIMIT, 1.0 / pivotCols.size()));
        }
        return pivotCols.stream()
                .map(ColumnVO::getField)
                .collect(toMap(
                        pivotCol -> pivotCol,
                        pivotCol -> getPivotValues(pivotCol, columnLimit),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    private List<String> getPivotValues(String pivotColumn, int limit) {
        return Collections.emptyList();
        // todo
        /*
        List<String> strings = template.queryForList(
                format("SELECT DISTINCT %s FROM trade", pivotColumn),
                String.class
        );
        if (strings.size() > limit) {
            logger.warn("Column \"{}\" has {} distinct values, only first {} will be used!", pivotColumn, strings.size(), limit);
            strings = strings.subList(0, limit);
        }
        return strings;*/
    }

    private List<Map<String, Object>> queryForList(String query) {
        query(query);
        // todo
        return Collections.emptyList();
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