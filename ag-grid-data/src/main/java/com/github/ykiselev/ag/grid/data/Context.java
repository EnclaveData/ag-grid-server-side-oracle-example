package com.github.ykiselev.ag.grid.data;

import com.github.ykiselev.ag.grid.api.request.AgGridGetRowsRequest;
import com.github.ykiselev.ag.grid.api.request.AggFunc;
import com.github.ykiselev.ag.grid.api.request.ColumnVO;
import com.github.ykiselev.ag.grid.api.response.AgGridGetRowsResponse;
import com.github.ykiselev.ag.grid.data.aggregation.Aggregation;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public final class Context {

    private final AgGridGetRowsRequest request;

    private final boolean isPivot;

    private final boolean isGrouping;

    private final List<String> valueColumns;

    private final List<String> groupByColumns;

    private final List<String> pivotColumns;

    private final Map<String, ColumnVO> columns;

    private final Set<String> columnsToMerge;

    private final Set<String> secondaryColumns = new HashSet<>();

    public boolean isPivot() {
        return isPivot;
    }

    public boolean isGrouping() {
        return isGrouping;
    }

    public List<String> getGroupByColumns() {
        return groupByColumns;
    }

    public List<String> getValueColumns() {
        return valueColumns;
    }

    public List<String> getPivotColumns() {
        return pivotColumns;
    }

    public boolean hasColumn(String name) {
        return columns.containsKey(name);
    }

    public ColumnVO getColumn(String fieldId) {
        return columns.get(fieldId);
    }

    public AgGridGetRowsRequest getRequest() {
        return request;
    }

    public int getGroupKeyCount() {
        return request.getGroupKeys().size();
    }

    public Set<String> getColumnsToMerge() {
        return columnsToMerge;
    }

    public Context(AgGridGetRowsRequest request, boolean isPivot, boolean isGrouping, List<String> valueColumns, List<String> groupByColumns, List<String> pivotColumns, Map<String, ColumnVO> columns) {
        this.request = requireNonNull(request);
        this.isPivot = isPivot;
        this.isGrouping = isGrouping;
        this.valueColumns = requireNonNull(valueColumns);
        this.groupByColumns = requireNonNull(groupByColumns);
        this.pivotColumns = requireNonNull(pivotColumns);
        this.columns = requireNonNull(columns);
        this.columnsToMerge = ImmutableSet.<String>builder()
                .addAll(valueColumns)
                .build();
    }

    public boolean addSecondaryColumn(String column) {
        return secondaryColumns.add(column);
    }

    /**
     * Secondary column names are generated during aggregation as a concatenation of pivot column values and value
     * column name. For example, having pivot columns A,B with values (a1,a2) and (b1) and value columns C,D we'll have
     * secondary columns "a1_b1_C", "a2_b1_C", "a1_b1_D", "a2_b2_D".<br/>
     * Number of columns is always multiplication of each column's number of values (Na * Nb)
     * <p/>
     * Note: this method is called from {@link Aggregation} class.
     *
     * @param columns the secondary column names to add
     * @return {@code true} if at least one of supplied columns was absent in this context.
     */
    public boolean addSecondaryColumns(Collection<String> columns) {
        return secondaryColumns.addAll(columns);
    }

    public AgGridGetRowsResponse createResponse(List<Map<String, Object>> rows) {
        final int currentLastRow = request.getStartRow() + rows.size();
        final int lastRow = currentLastRow <= request.getEndRow() ? currentLastRow : -1;
        return new AgGridGetRowsResponse<>(rows, lastRow, new ArrayList<>(secondaryColumns));
    }

    public Map<String, AggFunc> indexAggregationFunctions() {
        return getColumnsToMerge()
                .stream()
                .filter(col -> getColumn(col).getAggFunc() != null)
                .collect(Collectors.toMap(
                        Function.identity(),
                        col -> getColumn(col).getAggFunc()
                ));
    }

    public static Context create(AgGridGetRowsRequest request) {
        final ImmutableMap.Builder<String, ColumnVO> builder = ImmutableMap.builder();
        request.getRowGroupCols().forEach(c -> builder.put(c.getField(), c));
        request.getPivotCols().forEach(c -> builder.put(c.getField(), c));
        request.getValueCols().forEach(c -> builder.put(c.getField(), c));
        return new Context(
                request,
                request.isPivotMode() && !request.getPivotCols().isEmpty(),
                request.getRowGroupCols().size() > request.getGroupKeys().size(),
                ImmutableList.copyOf(
                        request.getValueCols()
                                .stream()
                                .map(ColumnVO::getField)
                                .toArray(String[]::new)
                ),
                ImmutableList.copyOf(
                        request.getRowGroupCols().stream()
                                .map(ColumnVO::getField)
                                .limit(request.getGroupKeys().size() + 1)
                                .toArray(String[]::new)
                ),
                ImmutableList.copyOf(
                        request.getPivotCols()
                                .stream()
                                .map(ColumnVO::getField)
                                .toArray(String[]::new)
                ),
                builder.build()
        );
    }
}
