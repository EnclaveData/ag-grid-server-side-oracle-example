package com.ag.grid.enterprise.oracle.demo.builder;

import com.github.ykiselev.ag.grid.api.request.ColumnVO;
import com.github.ykiselev.ag.grid.api.request.AgGridGetRowsRequest;
import com.github.ykiselev.ag.grid.api.response.AgGridGetRowsResponse;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.tuple.Pair;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.*;

public class EnterpriseResponseBuilder {

    public static AgGridGetRowsResponse createResponse(
            AgGridGetRowsRequest request,
            List<Map<String, Object>> rows,
            Map<String, List<String>> pivotValues) {

        int currentLastRow = request.getStartRow() + rows.size();
        int lastRow = currentLastRow <= request.getEndRow() ? currentLastRow : -1;

        List<ColumnVO> valueColumns = request.getValueCols();

        return new AgGridGetRowsResponse<>(rows, lastRow, getSecondaryColumns(pivotValues, valueColumns));
    }

    private static List<String> getSecondaryColumns(Map<String, List<String>> pivotValues, List<ColumnVO> valueColumns) {

        // create pairs of pivot col and pivot value i.e. (DEALTYPE,Financial), (BIDTYPE,Sell)...
        List<Set<Pair<String, String>>> pivotPairs = pivotValues.entrySet().stream()
                .map(e -> e.getValue().stream()
                        .map(pivotValue -> Pair.of(e.getKey(), pivotValue))
                        .collect(toCollection(LinkedHashSet::new)))
                .collect(toList());

        // create cartesian product of pivot and value columns i.e. Financial_Sell_CURRENTVALUE, Physical_Buy_CURRENTVALUE...
        return Sets.cartesianProduct(pivotPairs)
                .stream()
                .flatMap(pairs -> {
                    // collect pivot cols, i.e. Financial_Sell
                    String pivotCol = pairs.stream()
                            .map(Pair::getRight)
                            .collect(joining("_"));

                    // append value cols, i.e. Financial_Sell_CURRENTVALUE, Financial_Sell_PREVIOUSVALUE
                    return valueColumns.stream()
                            .map(valueCol -> pivotCol + "_" + valueCol.getField());
                })
                .collect(toList());
    }
}