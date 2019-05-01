package com.ag.grid.enterprise.oracle.demo;

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

/**
 * @author Yuriy Kiselev (uze@yandex.ru)
 * @since 20.04.2019
 */
public class ItemMapFactoryTest {

    @Test
    public void shouldPut() {
        Map<String, Object> map = new ItemMapFactory(4).get();

        map.put("a", "va");
        map.put("b", "vb");
        map.put("c", "vc");
        map.put("d", "vd");
        map.put("e", "ve");

        Assert.assertEquals(5, map.size());
    }
}