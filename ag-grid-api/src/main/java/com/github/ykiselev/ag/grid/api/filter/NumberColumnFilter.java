package com.github.ykiselev.ag.grid.api.filter;

public class NumberColumnFilter extends ColumnFilter {

    private String type;

    private Integer filter;

    private Integer filterTo;

    public NumberColumnFilter() {
    }

    public NumberColumnFilter(String type, Integer filter, Integer filterTo) {
        this.type = type;
        this.filter = filter;
        this.filterTo = filterTo;
    }

    public String getType() {
        return type;
    }

    public Integer getFilter() {
        return filter;
    }

    public Integer getFilterTo() {
        return filterTo;
    }

    @Override
    public String toString() {
        return "NumberColumnFilter{" +
                "type='" + type + '\'' +
                ", filter=" + filter +
                ", filterTo=" + filterTo +
                '}';
    }
}
