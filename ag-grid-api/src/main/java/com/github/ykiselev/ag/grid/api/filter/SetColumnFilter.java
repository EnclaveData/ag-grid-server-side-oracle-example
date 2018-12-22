package com.github.ykiselev.ag.grid.api.filter;

import java.util.Set;

public class SetColumnFilter extends ColumnFilter {

    private Set<String> values;

    public SetColumnFilter() {
    }

    public SetColumnFilter(Set<String> values) {
        this.values = values;
    }

    public Set<String> getValues() {
        return values;
    }

    @Override
    public String toString() {
        return "SetColumnFilter{" +
                "values=" + values +
                '}';
    }
}
