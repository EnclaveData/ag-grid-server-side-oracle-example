package com.github.ykiselev.ag.grid.api.response;

import java.util.List;

public class AgGridGetRowsResponse<V> {

    private List<V> data;

    private int lastRow;

    private List<String> secondaryColumnFields;

    public AgGridGetRowsResponse() {
    }

    public AgGridGetRowsResponse(List<V> data, int lastRow, List<String> secondaryColumnFields) {
        this.data = data;
        this.lastRow = lastRow;
        this.secondaryColumnFields = secondaryColumnFields;
    }

    public List<V> getData() {
        return data;
    }

    public void setData(List<V> data) {
        this.data = data;
    }

    public int getLastRow() {
        return lastRow;
    }

    public void setLastRow(int lastRow) {
        this.lastRow = lastRow;
    }

    public List<String> getSecondaryColumnFields() {
        return secondaryColumnFields;
    }

    public void setSecondaryColumns(List<String> secondaryColumnFields) {
        this.secondaryColumnFields = secondaryColumnFields;
    }
}