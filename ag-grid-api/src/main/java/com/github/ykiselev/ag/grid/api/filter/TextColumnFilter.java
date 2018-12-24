package com.github.ykiselev.ag.grid.api.filter;

public class TextColumnFilter extends ColumnFilter {

    private TextFilterType type;

    private String filter;

    public TextColumnFilter() {
    }

    public TextColumnFilter(TextFilterType type, String filter) {
        this.type = type;
        this.filter = filter;
    }

    public TextFilterType getType() {
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
