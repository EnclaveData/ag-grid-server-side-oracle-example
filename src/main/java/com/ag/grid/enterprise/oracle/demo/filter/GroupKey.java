package com.ag.grid.enterprise.oracle.demo.filter;

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
}
