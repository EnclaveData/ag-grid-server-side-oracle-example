package com.github.ykiselev.ag.grid.api.filter;

public class NumberColumnFilter extends ColumnFilter {

    private NumberFilterType type;

    private Integer filter;

    private Integer filterTo;

    public NumberColumnFilter() {
    }

    public NumberColumnFilter(NumberFilterType type, Integer filter, Integer filterTo) {
        this.type = type;
        this.filter = filter;
        this.filterTo = filterTo;
    }

    public NumberFilterType getType() {
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
