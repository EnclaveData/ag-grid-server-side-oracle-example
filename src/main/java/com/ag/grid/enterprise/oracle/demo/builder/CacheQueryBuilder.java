package com.ag.grid.enterprise.oracle.demo.builder;

import com.github.ykiselev.aggrid.domain.filter.ColumnFilter;
import com.github.ykiselev.aggrid.domain.filter.NumberColumnFilter;
import com.github.ykiselev.aggrid.domain.filter.SetColumnFilter;
import com.github.ykiselev.aggrid.domain.request.ColumnVO;
import com.github.ykiselev.aggrid.domain.request.AgGridGetRowsRequest;
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
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.common.collect.Streams.zip;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;

/**
 * Builds Oracle Coherence aggregator and filter from an EnterpriseGetRowsRequest.
 */
public final class CacheQueryBuilder {

    private static final Map<String, BiFunction<NumberColumnFilter, ValueExtractor, Filter>> numberFilterMap =
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

    private final AgGridGetRowsRequest request;

    private List<String> valueColumns;

    private List<String> pivotColumns;

    private Set<String> secondaryColumns;

    public CacheQueryBuilder(AgGridGetRowsRequest request) {
        this.request = requireNonNull(request);
    }

    public boolean isGrouping() {
        return getRowGroups().size() > request.getGroupKeys().size();
    }

    public boolean isPivot() {
        return request.isPivotMode() && !request.getPivotCols().isEmpty();
    }

    private List<String> getValueColumns() {
        if (valueColumns == null) {
            valueColumns = request.getValueCols()
                    .stream()
                    .map(ColumnVO::getField)
                    .collect(toList());
        }
        return valueColumns;
    }

    private List<String> getPivotColumns() {
        if (pivotColumns == null) {
            pivotColumns = request.getPivotCols()
                    .stream()
                    .map(ColumnVO::getField)
                    .collect(toList());
        }
        return pivotColumns;
    }

    public Set<String> getSecondaryColumns() {
        if (secondaryColumns == null) {
            secondaryColumns = new HashSet<>();
        }
        return secondaryColumns;
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
        final ValueExtractor[] rows = getRowGroupsToInclude()
                .stream()
                .map(group -> new ReflectionExtractor<>(getterFor(group)))
                .toArray(ValueExtractor[]::new);
        if (rows.length < 1) {
            throw new IllegalStateException("No row groups!");
        }
        final InvocableMap.EntryAggregator[] values = getValueAggregators();
        if (values.length < 1) {
            throw new IllegalStateException("No columns to aggregate!");
        }
        final GroupAggregator result;
        if (isPivot()) {
            result = createPivotAggregator(
                    rows,
                    request.getPivotCols()
                            .stream()
                            .map(col -> getterFor(col.getField()))
                            .map(ReflectionExtractor::new)
                            .toArray(ValueExtractor[]::new),
                    values
            );
        } else {
            result = GroupAggregator.createInstance(
                    rows.length > 1
                            ? new MultiExtractor(rows)
                            : rows[0],
                    values.length > 1
                            ? CompositeAggregator.createInstance(values)
                            : values[0]
            );
        }
        return result;
    }

    private GroupAggregator createPivotAggregator(ValueExtractor[] rows, ValueExtractor[] columns, InvocableMap.EntryAggregator[] values) {
        final CompositeAggregator valuesAggregator = CompositeAggregator.createInstance(values);

        InvocableMap.EntryAggregator columnsAndValuesAggregator;

        if (columns.length > 0) {
            InvocableMap.EntryAggregator columnsAggregator = valuesAggregator;

            // Iterate backwards over any columns wrapping each prior aggregator
            for (int columnIndex = columns.length - 1; columnIndex > 0; columnIndex--) {
//                columnsAggregator = CompositeAggregator.createInstance(new InvocableMap.EntryAggregator[]{
//                        valuesAggregator,
//                        GroupAggregator.createInstance(columns[columnIndex], columnsAggregator)
//                });
                columnsAggregator = GroupAggregator.createInstance(columns[columnIndex], columnsAggregator);
            }

            // Create a GroupAggregator for column[0]
            columnsAggregator = GroupAggregator.createInstance(columns[0], columnsAggregator);

            // Combine the columns and values aggregators together
            columnsAndValuesAggregator = columnsAggregator;
//            CompositeAggregator.createInstance(
//                    new InvocableMap.EntryAggregator[]{
//                            valuesAggregator,
//                            columnsAggregator
//                    }
//            );
        } else {
            // We have no columns so the rows will just use the values aggregator
            columnsAndValuesAggregator = valuesAggregator;
        }

        InvocableMap.EntryAggregator rowsAggregator = columnsAndValuesAggregator;

        // Iterate backwards over the rows wrapping each prior aggregator
        for (int rowIndex = rows.length - 1; rowIndex > 0; rowIndex--) {
            rowsAggregator = GroupAggregator.createInstance(rows[rowIndex], rowsAggregator);
//            rowsAggregator = CompositeAggregator.createInstance(
//                    new InvocableMap.EntryAggregator[]{
//                            columnsAndValuesAggregator,
//                            GroupAggregator.createInstance(rows[rowIndex], rowsAggregator)
//                    }
//            );
        }

        // Create a GroupAggregator for row[0]
        return GroupAggregator.createInstance(rows[0], rowsAggregator);
    }

    private InvocableMap.EntryAggregator[] getValueAggregators() {
        if (!isGrouping()) {
            throw new IllegalStateException("Not a grouping request!");
        }
        return request.getValueCols()
                .stream()
                .map(col -> aggregatorMap.get(col.getAggFunc()).apply(col.getField()))
                .toArray(InvocableMap.EntryAggregator[]::new);
    }

    public List<Map<String, Object>> parseResult(Object result) {
        if (result == null) {
            throw new NullPointerException("result");
        }
        if (result instanceof Map) {
            final Stream<Map<String, Object>> stream;
            if (isPivot()) {
                stream = ((Map<?, ?>) result).entrySet()
                        .parallelStream()
                        .map(this::toRows);
            } else {
                stream = ((Map<?, ?>) result).entrySet()
                        .parallelStream()
                        .map(this::toRow);
            }
            return stream.collect(toList());
        } else {
            throw new IllegalStateException("Unsupported result: " + result.getClass());
        }
    }

    private Map<String, Object> toRow(Map.Entry<?, ?> row) {
        final List<String> keyColumns = getRowGroupsToInclude();
        final List<String> valueColumns = getValueColumns();
        final Map<String, Object> result = new HashMap<>(keyColumns.size() + valueColumns.size());
        addTo(result, keyColumns, row.getKey());
        addTo(result, valueColumns, row.getValue());
        return result;
    }

    private String getColumnName(int index) {
        final List<String> keys = getRowGroupsToInclude();
        if (index < keys.size()) {
            return keys.get(index);
        }
        return getPivotColumns().get(index - keys.size());
    }

    private Map<String, Object> toRows(Map.Entry<?, ?> row) {
        final Map<String, Object> result = new HashMap<>();
        append(0, new Node(null, getColumnName(0), row.getKey(), null), row.getValue(), result);
        return result;
    }

    private static String name(String parentName, String name) {
        if (parentName != null) {
            return parentName + "_" + name;
        }
        return name;
    }

    private void append(int index, Node node, Object value, Map<String, Object> target) {
        if (value instanceof Map) {
            final int idx = index + 1;
            final String name = getColumnName(idx);
            final Function<Object, String> path;
            if (idx <= request.getGroupKeys().size()) {
                path = Objects::toString;
            } else {
                path = k -> name(node.getPath(), Objects.toString(k));
            }
            ((Map<String, Object>) value).forEach((k, v) ->
                    append(idx, new Node(node, name, k, path.apply(k)), v, target));
        } else {
            append(node, value, target);
        }
    }

    private void append(Node parent, Object value, Map<String, Object> target) {
        Node n = parent;
        while (n != null) {
            target.put(n.getName(), n.getKey());
            n = n.getParent();
        }
        final List<String> secondaryColumns = getValueColumns()
                .stream()
                .map(col -> parent.getPath() + "_" + col)
                .collect(toList());
        getSecondaryColumns().addAll(secondaryColumns);
        addTo(target, secondaryColumns, value);
    }

    private static void addTo(Map<String, Object> target, List<String> keys, Object values) {
        if (values instanceof List) {
            addTo(target, keys, (List<?>) values);
        } else if (keys.size() == 1) {
            target.put(keys.get(0), values);
        } else {
            throw new IllegalStateException("Unsupported type: " + values);
        }
    }

    private static void addTo(Map<String, Object> target, List<String> keys, List<?> values) {
        if (values.size() != keys.size()) {
            throw new IllegalStateException("Size mismatch: got " + values.size() + " values and " + keys.size() + " keys");
        }
        for (int i = 0; i < keys.size(); i++) {
            target.put(keys.get(i), values.get(i));
        }
    }

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
        final Function<Map.Entry<String, ColumnFilter>, Filter> applyFilters = entry -> {
            String columnName = entry.getKey();
            ColumnFilter filter = entry.getValue();

            if (filter instanceof SetColumnFilter) {
                return setFilter(columnName, (SetColumnFilter) filter);
            }
            if (filter instanceof NumberColumnFilter) {
                return numberFilter(columnName, (NumberColumnFilter) filter);
            }
            return AlwaysFilter.INSTANCE;
        };

        return request.getFilterModel().entrySet().stream().map(applyFilters);
    }

    // todo - limited draft code!
    private static String getterFor(String field) {
        return "get" + Character.toUpperCase(field.charAt(0)) + field.substring(1);
    }

    private Filter setFilter(String columnName, SetColumnFilter filter) {
        return new InFilter<>(getterFor(columnName),
                filter.getValues().isEmpty()
                        ? Collections.emptySet()
                        : new HashSet<>(filter.getValues())
        );
    }

    private Filter numberFilter(String columnName, NumberColumnFilter filter) {
        return numberFilterMap.get(filter.getType())
                .apply(filter, new ReflectionExtractor<>(getterFor(columnName)));
    }

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

}

final class Node {

    private Node parent;

    private final String path;

    private final String name;

    private final Object key;

    String getPath() {
        return path;
    }

    public Node getParent() {
        return parent;
    }

    String getName() {
        return name;
    }

    public Object getKey() {
        return key;
    }

    Node(Node parent, String name, Object key, String path) {
        this.parent = parent;
        this.name = name;
        this.key = key;
        this.path = path;
    }
}
