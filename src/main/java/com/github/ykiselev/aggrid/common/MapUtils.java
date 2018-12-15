package com.github.ykiselev.aggrid.common;

import java.util.Map;
import java.util.function.Function;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public final class MapUtils {

    public static Function<Map<String, Object>, Comparable> extractValue(String key) {
        return (Map<String, Object> m) -> (Comparable) m.get(key);
    }
}
