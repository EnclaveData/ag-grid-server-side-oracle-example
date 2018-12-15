package com.github.ykiselev.aggrid.common;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public final class MapUtils {

    public static Function<Map<String, Object>, String> extractValue(String key) {
        return (Map<String, Object> m) -> Objects.toString(m.get(key));
    }
}
