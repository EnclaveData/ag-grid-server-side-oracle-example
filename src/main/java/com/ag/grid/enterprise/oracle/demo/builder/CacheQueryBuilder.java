package com.ag.grid.enterprise.oracle.demo.builder;

import com.ag.grid.enterprise.oracle.demo.filter.ColumnFilter;
import com.ag.grid.enterprise.oracle.demo.filter.NumberColumnFilter;
import com.ag.grid.enterprise.oracle.demo.filter.SetColumnFilter;
import com.ag.grid.enterprise.oracle.demo.request.ColumnVO;
import com.ag.grid.enterprise.oracle.demo.request.EnterpriseGetRowsRequest;
import com.google.common.collect.ImmutableMap;
import com.tangosol.util.Filter;
import com.tangosol.util.InvocableMap;
import com.tangosol.util.ValueExtractor;
import com.tangosol.util.aggregator.CompositeAggregator;
import com.tangosol.util.aggregator.DoubleAverage;
import com.tangosol.util.aggregator.DoubleMax;
import com.tangosol.util.aggregator.DoubleMin;
import com.tangosol.util.aggregator.DoubleSum;
import com.tangosol.util.aggregator.GroupAggregator;
import com.tangosol.util.extractor.MultiExtractor;
import com.tangosol.util.extractor.ReflectionExtractor;
import com.tangosol.util.filter.AllFilter;
import com.tangosol.util.filter.AlwaysFilter;
import com.tangosol.util.filter.EqualsFilter;
import com.tangosol.util.filter.InFilter;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.common.collect.Streams.zip;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;

/**
 * Builds Oracle SQL queries from an EnterpriseGetRowsRequest.
 */
public final class CacheQueryBuilder {

    private static final Map<String, BiFunction<NumberColumnFilter, ValueExtractor, Filter>> filterMap =
            ImmutableMap.<String, BiFunction<NumberColumnFilter, ValueExtractor, Filter>>builder()
                    .put("inRange", Filters::between)
                    .put("equals", Filters::equals)
                    .put("notEqual", Filters::notEquals)
                    .put("lessThan", Filters::lessThan)
                    .put("lessThanOrEqual", Filters::lessThanOrEqual)
                    .put("greaterThan", Filters::greaterThan)
                    .put("greaterThanOrEqual", Filters::greaterThanOrEqual)
                    .build();

    private static final Map<String, Function<String, InvocableMap.EntryAggregator>> aggregatorMap = ImmutableMap.of(
            "avg", col -> new DoubleAverage(getterFor(col)),
            "sum", col -> new DoubleSum(getterFor(col)),
            "min", col -> new DoubleMin(getterFor(col)),
            "max", col -> new DoubleMax(getterFor(col))
    );

    private List<String> rowGroups;

    private List<String> rowGroupsToInclude;

    private Map<String, List<String>> pivotValues;

    private final EnterpriseGetRowsRequest request;

    public CacheQueryBuilder(EnterpriseGetRowsRequest request) {
        this.request = requireNonNull(request);
    }

    public boolean isGrouping() {
        return getRowGroups().size() > request.getGroupKeys().size();
    }

    public boolean isPivot() {
        return request.isPivotMode() && !request.getPivotCols().isEmpty();
    }

    public Filter filter() {
        final Filter[] filters = concat(getGroupColumns(), getFilters()).toArray(Filter[]::new);
        if (filters.length > 0) {
            return new AllFilter(filters);
        }
        return AlwaysFilter.INSTANCE;
    }

    public GroupAggregator groupAggregator() {
        if (!isGrouping()) {
            throw new IllegalStateException("Not a grouping request!");
        }
        final ValueExtractor[] extractors = getRowGroupsToInclude()
                .stream()
                .map(group -> new ReflectionExtractor<>(getterFor(group)))
                .toArray(ValueExtractor[]::new);
        if (extractors.length < 1) {
            throw new IllegalStateException("No row groups!");
        }
        final InvocableMap.EntryAggregator[] entryAggregators = getGroupEntryAggregators();
        if (entryAggregators.length < 1) {
            throw new IllegalStateException("No columns to aggregate!");
        }
        return GroupAggregator.createInstance(
                extractors.length > 1
                        ? new MultiExtractor(extractors)
                        : extractors[0],
                entryAggregators.length > 1
                        ? CompositeAggregator.createInstance(entryAggregators)
                        : entryAggregators[0]
        );
    }

    private InvocableMap.EntryAggregator[] getGroupEntryAggregators() {
        if (!isGrouping()) {
            throw new IllegalStateException("Not a grouping request!");
        }
        Stream<InvocableMap.EntryAggregator> aggregators;
        if (isPivot()) {
            aggregators = Stream.empty();// todo - concat(rowGroupsToInclude.stream(), extractPivotStatements());
        } else {
            aggregators = request.getValueCols()
                    .stream()
                    .map(col -> aggregatorMap.get(col.getAggFunc()).apply(col.getField()));
        }

        return aggregators.toArray(InvocableMap.EntryAggregator[]::new);
    }

    public List<Map<String, Object>> parseResult(Object result) {
        if (result == null) {
            throw new NullPointerException("result");
        }
        if (result instanceof Map) {
            final List<String> keyColumns = getRowGroupsToInclude();
            final List<String> valueColumns;
            if (isPivot()) {
                throw new UnsupportedOperationException("Not implemented!");
            } else {
                valueColumns = request.getValueCols()
                        .stream()
                        .map(ColumnVO::getField)
                        .collect(toList());
            }
            final Map<?, ?> map = (Map<?, ?>) result;
            return map.entrySet()
                    .parallelStream()
                    .map(e -> rowToMap(e, keyColumns, valueColumns))
                    .collect(toList());
        } else {
            throw new IllegalStateException("Unsupported result: " + result.getClass());
        }
    }

    private static Map<String, Object> rowToMap(Map.Entry<?, ?> row, List<String> key, List<String> values) {
        final Map<String, Object> result = new HashMap<>(key.size() + values.size());
        addTo(result, key, row.getKey());
        addTo(result, values, row.getValue());
        return result;
    }

    private static void addTo(Map<String, Object> target, List<String> keys, Object values) {
        if (values instanceof List) {
            addTo(target, keys, (List<?>) values);
            final List<?> valueList = (List<?>) values;
            if (valueList.size() != keys.size()) {
                throw new IllegalStateException("Key size mismatch: " + valueList.size() + " <> " + keys.size());
            }
            for (int i = 0; i < keys.size(); i++) {
                target.put(keys.get(i), valueList.get(i));
            }
        } else if (keys.size() == 1) {
            target.put(keys.get(0), values);
        } else {
            throw new IllegalStateException("Unsupported type: " + values);
        }
    }

    private static void addTo(Map<String, Object> target, List<String> keys, List<?> values) {
        if (values.size() != keys.size()) {
            throw new IllegalStateException("Key size mismatch: " + values.size() + " <> " + keys.size());
        }
        for (int i = 0; i < keys.size(); i++) {
            target.put(keys.get(i), values.get(i));
        }
    }

//    public String createSql(EnterpriseGetRowsRequest request, String tableName, Map<String, List<String>> pivotValues) {
//        this.pivotValues = pivotValues;
//        this.rowGroups = getRowGroups();
//        this.rowGroupsToInclude = getRowGroupsToInclude();
//        this.isGrouping = rowGroups.size() > request.getGroupKeys().size();
//
//        return selectSql() + fromSql(tableName) + whereSql() + groupBySql() + orderBySql();// + limitSql();
//    }

//    private InvocableMap.EntryAggregator[] getSelectAggregators() {
//        List<InvocableMap.EntryAggregator> aggregators;
//        if (isGrouping) {
//            if (request.isPivotMode() && !request.getPivotCols().isEmpty()) {
//                aggregators = concat(rowGroupsToInclude.stream(), extractPivotStatements());
//            } else {
//                Stream<String> valueCols = request.getValueCols().stream()
//                        .map(valueCol -> valueCol.getAggFunc() + '(' + valueCol.getField() + ") ");// + valueCol.getField());
//
//                aggregators = concat(rowGroupsToInclude.stream(), valueCols);
//            }
//        } else {
//            // "select *"
//            aggregators = Stream.of("product", "portfolio", "book", "tradeId", "submitterId", "submitterDealId", "dealType",
//                    "bidType", "currentValue", "previousValue", "pl1", "pl2", "gainDx", "sxPx", "x99Out", "batch")
//                    .map();
//        }
//        return aggregators.toArray(InvocableMap.EntryAggregator[]::new);
//    }

//    private String selectSql() {
//        List<String> selectCols;
//        if (request.isPivotMode() && !request.getPivotCols().isEmpty()) {
//            selectCols = concat(rowGroupsToInclude.stream(), extractPivotStatements()).collect(toList());
//        } else {
//            Stream<String> valueCols = valueColumns.stream()
//                    .map(valueCol -> valueCol.getAggFunc() + '(' + valueCol.getField() + ") ");// + valueCol.getField());
//
//            selectCols = concat(rowGroupsToInclude.stream(), valueCols).collect(toList());
//        }
//
//        return isGrouping ? "SELECT " + join(", ", selectCols) : "SELECT *";
//    }

//    private String fromSql(String tableName) {
//        return format(" FROM %s", tableName);
//    }

//    private String whereSql() {
//        String whereFilters =
//                concat(getGroupColumns(), getFilters())
//                        .collect(joining(" AND "));
//
//        return whereFilters.isEmpty() ? "" : format(" WHERE %s", whereFilters);
//    }

//    private String groupBySql() {
//        return isGrouping ? " GROUP BY " + join(", ", rowGroupsToInclude) : "";
//    }

    // todo
//    private String orderBySql() {
//        Function<SortModel, String> orderByMapper = model -> model.getColId() + " " + model.getSort();
//
//        boolean isDoingGrouping = getRowGroups().size() > groupKeys.size();
//        int num = isDoingGrouping ? groupKeys.size() + 1 : MAX_VALUE;
//
//        List<String> orderByCols = sortModel.stream()
//                .filter(model -> !isDoingGrouping || getRowGroups().contains(model.getColId()))
//                .map(orderByMapper)
//                .limit(num)
//                .collect(toList());
//
//        return orderByCols.isEmpty() ? "" : " ORDER BY " + join(",", orderByCols);
//    }

    // todo
//    private String limitSql() {
//        return " OFFSET " + startRow + " ROWS FETCH NEXT " + (endRow - startRow + 1) + " ROWS ONLY";
//    }

    private Stream<Filter> getFilters() {
        Function<Map.Entry<String, ColumnFilter>, Filter> applyFilters = entry -> {
            String columnName = entry.getKey();
            ColumnFilter filter = entry.getValue();

            if (filter instanceof SetColumnFilter) {
                return setFilter().apply(columnName, (SetColumnFilter) filter);
            }

            if (filter instanceof NumberColumnFilter) {
                return numberFilter().apply(columnName, (NumberColumnFilter) filter);
            }

            return AlwaysFilter.INSTANCE;
        };

        return request.getFilterModel().entrySet().stream().map(applyFilters);
    }

    // todo - limited draft code!
    public static String getterFor(String field) {
        return "get" + Character.toUpperCase(field.charAt(0)) + field.substring(1);
    }

    private BiFunction<String, SetColumnFilter, Filter> setFilter() {
        return (String columnName, SetColumnFilter filter) ->
                new InFilter<>(getterFor(columnName),
                        filter.getValues().isEmpty()
                                ? Collections.emptySet()
                                : new HashSet<>(filter.getValues())
                );
    }

    private BiFunction<String, NumberColumnFilter, Filter> numberFilter() {
        return (String columnName, NumberColumnFilter filter) ->
                filterMap.get(filter.getType())
                        .apply(filter, new ReflectionExtractor<>(getterFor(columnName)));
    }

    // todo
//    private Stream<String> extractPivotStatements() {
//
//        // create pairs of pivot col and pivot value i.e. (DEALTYPE,Financial), (BIDTYPE,Sell)...
//        List<Set<Pair<String, String>>> pivotPairs = pivotValues.entrySet().stream()
//                .map(e -> e.getValue().stream()
//                        .map(pivotValue -> Pair.of(e.getKey(), pivotValue))
//                        .collect(toCollection(LinkedHashSet::new)))
//                .collect(toList());
//
//        // create a cartesian product of decode statements for all pivot and value columns combinations
//        // i.e. sum(DECODE(DEALTYPE, 'Financial', DECODE(BIDTYPE, 'Sell', CURRENTVALUE)))
//        return Sets.cartesianProduct(pivotPairs)
//                .stream()
//                .flatMap(pairs -> {
//                    String pivotColStr = pairs.stream()
//                            .map(Pair::getRight)
//                            .collect(joining("_"));
//
//                    String decodeStr = pairs.stream()
//                            .map(pair -> "DECODE(" + pair.getLeft() + ", '" + pair.getRight() + "'")
//                            .collect(joining(", "));
//
//                    String closingBrackets = IntStream
//                            .range(0, pairs.size() + 1)
//                            .mapToObj(i -> ")")
//                            .collect(joining(""));
//
//                    return valueColumns.stream()
//                            .map(valueCol -> valueCol.getAggFunc() + "(" + decodeStr + ", " + valueCol.getField() +
//                                    closingBrackets + " \"" + pivotColStr + "_" + valueCol.getField() + "\"");
//                });
//    }

    private List<String> getRowGroupsToInclude() {
        if (rowGroupsToInclude == null) {
            rowGroupsToInclude = getRowGroups().stream()
                    .limit(request.getGroupKeys().size() + 1)
                    .collect(toList());
        }
        return rowGroupsToInclude;
    }

    // todo what if group column type is not string? Coherence doesn't convert types!
    private Stream<Filter> getGroupColumns() {
        return zip(request.getGroupKeys().stream(), getRowGroups().stream(), (key, group) -> new EqualsFilter<>(getterFor(group), key));
    }

    private List<String> getRowGroups() {
        if (rowGroups == null) {
            rowGroups = request.getRowGroupCols().stream()
                    .map(ColumnVO::getField)
                    .collect(toList());
        }
        return rowGroups;
    }

//    private String asString(List<String> l) {
//        return "(" + l.stream().map(s -> "\'" + s + "\'").collect(joining(", ")) + ")";
//    }

}