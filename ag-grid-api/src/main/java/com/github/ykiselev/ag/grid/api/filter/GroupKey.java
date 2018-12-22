package com.github.ykiselev.ag.grid.api.filter;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public final class GroupKey extends ColumnFilter {

    private final String filter;

    public String getFilter() {
        return filter;
    }

    public GroupKey(String filter) {
        this.filter = filter;
    }

    @Override
    public String toString() {
        return "GroupKey{" +
                "filter='" + filter + '\'' +
                '}';
    }
}
