package com.ag.grid.enterprise.oracle.demo;

import com.ag.grid.enterprise.oracle.demo.builder.OracleSqlQueryBuilder;
import com.github.ykiselev.ag.grid.api.filter.ColumnFilter;
import com.github.ykiselev.ag.grid.api.filter.NumberColumnFilter;
import com.github.ykiselev.ag.grid.api.filter.NumberFilterType;
import com.github.ykiselev.ag.grid.api.filter.SetColumnFilter;
import com.github.ykiselev.ag.grid.api.request.AggFunc;
import com.github.ykiselev.ag.grid.api.request.ColumnVO;
import com.github.ykiselev.ag.grid.api.request.AgGridGetRowsRequest;
import com.github.ykiselev.ag.grid.api.request.SortModel;
import com.github.ykiselev.ag.grid.api.request.Sorting;
import com.google.common.collect.ImmutableSet;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

@Ignore
public class OracleSqlQueryBuilderTest {

    @Test
    public void singleGroup() {
        AgGridGetRowsRequest request = new AgGridGetRowsRequest();
        request.setStartRow(0);
        request.setEndRow(100);
        request.setRowGroupCols(singletonList(
                new ColumnVO("COUNTRY", "Country", "COUNTRY", null)
        ));
        request.setValueCols(asList(
                new ColumnVO("GOLD", "Gold", "GOLD", AggFunc.SUM),
                new ColumnVO("SILVER", "Silver", "SILVER", AggFunc.SUM),
                new ColumnVO("BRONZE", "Bronze", "BRONZE", AggFunc.SUM),
                new ColumnVO("TOTAL", "Total", "TOTAL", AggFunc.SUM)
        ));

        String sql = new OracleSqlQueryBuilder().createSql(request, "medal", emptyMap());

        assertEquals("SELECT COUNTRY, sum(GOLD) as GOLD, sum(SILVER) as SILVER, sum(BRONZE) as BRONZE, " +
                "sum(TOTAL) as TOTAL FROM medal GROUP BY COUNTRY OFFSET 0 ROWS FETCH NEXT 101 ROWS ONLY", sql);
    }

    @Test
    public void multipleGroups() {
        AgGridGetRowsRequest request = new AgGridGetRowsRequest();
        request.setStartRow(100);
        request.setEndRow(200);
        request.setRowGroupCols(asList(
                new ColumnVO("COUNTRY", "Country", "COUNTRY", null),
                new ColumnVO("YEAR", "Year", "YEAR", null)
        ));
        request.setValueCols(asList(
                new ColumnVO("GOLD", "Gold", "GOLD", AggFunc.SUM),
                new ColumnVO("SILVER", "Silver", "SILVER", AggFunc.SUM),
                new ColumnVO("BRONZE", "Bronze", "BRONZE", AggFunc.SUM),
                new ColumnVO("TOTAL", "Total", "TOTAL", AggFunc.SUM)
        ));

        String sql = new OracleSqlQueryBuilder().createSql(request, "medal", emptyMap());

        assertEquals("SELECT COUNTRY, sum(GOLD) as GOLD, sum(SILVER) as SILVER, sum(BRONZE) as BRONZE, " +
                "sum(TOTAL) as TOTAL FROM medal GROUP BY COUNTRY OFFSET 100 ROWS FETCH NEXT 101 ROWS ONLY", sql);
    }

    @Test
    public void twoGroupsWithGroupKey() {
        AgGridGetRowsRequest request = new AgGridGetRowsRequest();
        request.setStartRow(100);
        request.setEndRow(200);
        request.setRowGroupCols(asList(
                new ColumnVO("COUNTRY", "Country", "COUNTRY", null),
                new ColumnVO("YEAR", "Year", "YEAR", null)
        ));
        request.setValueCols(asList(
                new ColumnVO("GOLD", "Gold", "GOLD", AggFunc.SUM),
                new ColumnVO("SILVER", "Silver", "SILVER", AggFunc.SUM),
                new ColumnVO("BRONZE", "Bronze", "BRONZE", AggFunc.SUM),
                new ColumnVO("TOTAL", "Total", "TOTAL", AggFunc.SUM)
        ));
        request.setGroupKeys(singletonList("Denmark"));

        String sql = new OracleSqlQueryBuilder().createSql(request, "medal", emptyMap());

        assertEquals("SELECT COUNTRY, YEAR, sum(GOLD) as GOLD, sum(SILVER) as SILVER, sum(BRONZE) as BRONZE, " +
                "sum(TOTAL) as TOTAL FROM medal WHERE COUNTRY = 'Denmark' GROUP BY COUNTRY, YEAR " +
                "OFFSET 100 ROWS FETCH NEXT 101 ROWS ONLY", sql);
    }

    @Test
    public void singleGroupWithFilteringAndSorting() {
        AgGridGetRowsRequest request = new AgGridGetRowsRequest();
        request.setStartRow(0);
        request.setEndRow(100);
        request.setRowGroupCols(singletonList(
                new ColumnVO("COUNTRY", "Country", "COUNTRY", null)
        ));
        request.setValueCols(asList(
                new ColumnVO("GOLD", "Gold", "GOLD", AggFunc.SUM),
                new ColumnVO("SILVER", "Silver", "SILVER", AggFunc.SUM),
                new ColumnVO("BRONZE", "Bronze", "BRONZE", AggFunc.SUM),
                new ColumnVO("TOTAL", "Total", "TOTAL", AggFunc.SUM)
        ));

        request.setFilterModel(new HashMap<String, ColumnFilter>() {{
            put("SPORT", new SetColumnFilter(ImmutableSet.of("Rowing", "Tennis")));
            put("AGE", new NumberColumnFilter(NumberFilterType.EQUALS, 22, null));
        }});
        request.setSortModel(singletonList(new SortModel("ATHLETE", Sorting.ASC)));

        String sql = new OracleSqlQueryBuilder().createSql(request, "medal", emptyMap());

        assertEquals("SELECT COUNTRY, sum(GOLD) as GOLD, sum(SILVER) as SILVER, sum(BRONZE) as BRONZE, " +
                "sum(TOTAL) as TOTAL FROM medal WHERE SPORT IN ('Rowing', 'Tennis') AND AGE = 22 GROUP BY COUNTRY " +
                "OFFSET 0 ROWS FETCH NEXT 101 ROWS ONLY", sql);
    }

    @Test
    public void pivotModeNoPivotCols() {
        AgGridGetRowsRequest request = new AgGridGetRowsRequest();
        request.setStartRow(0);
        request.setEndRow(100);
        request.setRowGroupCols(singletonList(
                new ColumnVO("COUNTRY", "Country", "COUNTRY", null)
        ));
        request.setValueCols(asList(
                new ColumnVO("GOLD", "Gold", "GOLD", AggFunc.SUM),
                new ColumnVO("SILVER", "Silver", "SILVER", AggFunc.SUM),
                new ColumnVO("BRONZE", "Bronze", "BRONZE", AggFunc.SUM),
                new ColumnVO("TOTAL", "Total", "TOTAL", AggFunc.SUM)
        ));
        request.setPivotMode(true);

        String sql = new OracleSqlQueryBuilder().createSql(request, "medal", emptyMap());

        assertEquals("SELECT COUNTRY, sum(GOLD) as GOLD, sum(SILVER) as SILVER, sum(BRONZE) as BRONZE, " +
                "sum(TOTAL) as TOTAL FROM medal GROUP BY COUNTRY OFFSET 0 ROWS FETCH NEXT 101 ROWS ONLY", sql);
    }

    @Test
    public void pivotModeWithSinglePivotCol() {
        AgGridGetRowsRequest request = new AgGridGetRowsRequest();
        request.setStartRow(0);
        request.setEndRow(100);
        request.setRowGroupCols(singletonList(
                new ColumnVO("COUNTRY", "Country", "COUNTRY", null)
        ));
        request.setValueCols(asList(
                new ColumnVO("GOLD", "Gold", "GOLD", AggFunc.SUM),
                new ColumnVO("SILVER", "Silver", "SILVER", AggFunc.SUM),
                new ColumnVO("BRONZE", "Bronze", "BRONZE", AggFunc.SUM),
                new ColumnVO("TOTAL", "Total", "TOTAL", AggFunc.SUM)
        ));
        request.setPivotMode(true);
        request.setPivotCols(singletonList(
                new ColumnVO("SPORT", "Sport", "SPORT", null)
        ));

        Map<String, List<String>> pivotValues = new HashMap<>();
        pivotValues.put("SPORT", asList("Athletics", "Speed Skating"));
        pivotValues.put("YEAR", asList("2000", "2004"));

        String sql = new OracleSqlQueryBuilder().createSql(request, "medal", pivotValues);

        assertEquals("SELECT COUNTRY, sum(DECODE(SPORT, 'Athletics', DECODE(YEAR, '2000', GOLD))) \"Athletics_2000_GOLD\"," +
                " sum(DECODE(SPORT, 'Athletics', DECODE(YEAR, '2000', SILVER))) \"Athletics_2000_SILVER\", sum(DECODE(SPORT," +
                " 'Athletics', DECODE(YEAR, '2000', BRONZE))) \"Athletics_2000_BRONZE\", sum(DECODE(SPORT, 'Athletics'," +
                " DECODE(YEAR, '2000', TOTAL))) \"Athletics_2000_TOTAL\", sum(DECODE(SPORT, 'Athletics', DECODE(YEAR," +
                " '2004', GOLD))) \"Athletics_2004_GOLD\", sum(DECODE(SPORT, 'Athletics', DECODE(YEAR, '2004', SILVER)))" +
                " \"Athletics_2004_SILVER\", sum(DECODE(SPORT, 'Athletics', DECODE(YEAR, '2004', BRONZE)))" +
                " \"Athletics_2004_BRONZE\", sum(DECODE(SPORT, 'Athletics', DECODE(YEAR, '2004', TOTAL))) \"Athletics_2004_TOTAL\"," +
                " sum(DECODE(SPORT, 'Speed Skating', DECODE(YEAR, '2000', GOLD))) \"Speed Skating_2000_GOLD\", sum(DECODE(SPORT," +
                " 'Speed Skating', DECODE(YEAR, '2000', SILVER))) \"Speed Skating_2000_SILVER\", sum(DECODE(SPORT, 'Speed Skating'," +
                " DECODE(YEAR, '2000', BRONZE))) \"Speed Skating_2000_BRONZE\", sum(DECODE(SPORT, 'Speed Skating', DECODE(YEAR," +
                " '2000', TOTAL))) \"Speed Skating_2000_TOTAL\", sum(DECODE(SPORT, 'Speed Skating', DECODE(YEAR, '2004', GOLD)))" +
                " \"Speed Skating_2004_GOLD\", sum(DECODE(SPORT, 'Speed Skating', DECODE(YEAR, '2004', SILVER))) " +
                "\"Speed Skating_2004_SILVER\", sum(DECODE(SPORT, 'Speed Skating', DECODE(YEAR, '2004', BRONZE))) " +
                "\"Speed Skating_2004_BRONZE\", sum(DECODE(SPORT, 'Speed Skating', DECODE(YEAR, '2004', TOTAL))) " +
                "\"Speed Skating_2004_TOTAL\" FROM medal GROUP BY COUNTRY OFFSET 0 ROWS FETCH NEXT 101 ROWS ONLY", sql);
    }
}