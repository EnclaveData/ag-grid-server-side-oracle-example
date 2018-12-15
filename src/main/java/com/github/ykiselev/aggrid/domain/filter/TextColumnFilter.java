package com.github.ykiselev.aggrid.domain.filter;

public class TextColumnFilter extends ColumnFilter {

    private String type;

    private String filter;

    public TextColumnFilter() {
    }

    public TextColumnFilter(String type, String filter) {
        this.type = type;
        this.filter = filter;
    }

    public String getType() {
        return type;
    }

    public String getFilter() {
        return filter;
    }

    @Override
    public String toString() {
        return "TextColumnFilter{" +
                "type='" + type + '\'' +
                ", filter='" + filter + '\'' +
                '}';
    }
}
