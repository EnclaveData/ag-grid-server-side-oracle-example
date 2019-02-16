package com.github.ykiselev.ag.grid.data;

/**
 * @author Yuriy Kiselev (uze@yandex.ru)
 * @since 20.01.2019
 */
public final class GetRowsStats {

    private final int totalObjects;

    private final int filteredObjects;

    private final int aggregatedObjects;

    public int getAggregatedObjects() {
        return aggregatedObjects;
    }

    public int getTotalObjects() {
        return totalObjects;
    }

    public int getFilteredObjects() {
        return filteredObjects;
    }

    public GetRowsStats(int totalObjects, int filteredObjects, int aggregatedObjects) {
        this.totalObjects = totalObjects;
        this.filteredObjects = filteredObjects;
        this.aggregatedObjects = aggregatedObjects;
    }

    @Override
    public String toString() {
        return "GetRowsStats{" +
                "totalObjects=" + totalObjects +
                ", filteredObjects=" + filteredObjects +
                ", aggregatedObjects=" + aggregatedObjects +
                '}';
    }
}
