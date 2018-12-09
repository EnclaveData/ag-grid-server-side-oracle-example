package com.ag.grid.enterprise.oracle.demo.data.types;

import java.util.Set;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public interface TypeInfo {

    Set<String> getNames();

    Class<?> getType(String name);

}
