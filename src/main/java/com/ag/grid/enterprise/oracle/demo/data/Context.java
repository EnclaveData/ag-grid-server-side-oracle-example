package com.ag.grid.enterprise.oracle.demo.data;

import com.ag.grid.enterprise.oracle.demo.request.AgGridGetRowsRequest;
import com.ag.grid.enterprise.oracle.demo.request.ColumnVO;
import com.ag.grid.enterprise.oracle.demo.response.AgGridGetRowsResponse;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public Context(AgGridGetRowsRequest request, boolean isPivot, boolean isGrouping, List<String> valueColumns, List<String> groupByColumns, List<String> pivotColumns, Map<String, ColumnVO> columns) {
        this.request = requireNonNull(request);
        this.isPivot = isPivot;
        this.isGrouping = isGrouping;
        this.valueColumns = requireNonNull(valueColumns);
        this.groupByColumns = requireNonNull(groupByColumns);
        this.pivotColumns = requireNonNull(pivotColumns);
        this.columns = requireNonNull(columns);
    }

    public boolean addSecondaryColumn(String column) {
        return secondaryColumns.add(column);
    }

    public boolean addSecondaryColumns(Collection<String> columns) {
        return secondaryColumns.addAll(columns);
    }

    public AgGridGetRowsResponse createResponse(List<Map<String, Object>> rows) {
        final int currentLastRow = request.getStartRow() + rows.size();
        final int lastRow = currentLastRow <= request.getEndRow() ? currentLastRow : -1;
        return new AgGridGetRowsResponse(rows, lastRow, new ArrayList<>(secondaryColumns));
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
