package com.ag.grid.enterprise.oracle.demo;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.Weigher;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Yuriy Kiselev (uze@yandex.ru)
 * @since 25.04.2019
 */
public class GuavaCacheTest {

    interface Weightable {

        int getWeight();
    }

    static class Item implements Weightable {

        private final int payload;

        private final int weight;

        public Item(int payload, int weight) {
            this.payload = payload;
            this.weight = weight;
        }

        @Override
        public int getWeight() {
            return weight;
        }

        @Override
        public String toString() {
            return "Item{" +
                    "payload=" + payload +
                    ", weight=" + weight +
                    '}';
        }
    }

    @Test
    public void shouldCacheAndEvict() {
        final Cache<String, Weightable> cache = CacheBuilder.newBuilder()
                .concurrencyLevel(1)
                .expireAfterAccess(100, TimeUnit.SECONDS)
                .maximumWeight(10_000)
                .weigher((Weigher<String, Weightable>) (key, value) -> value.getWeight())
                .removalListener((RemovalListener<String, Object>) notification ->
                        System.out.println(notification.getKey() + " : " + notification.getValue() + " was " + (notification.wasEvicted() ? "evicted" : "removed") + " due to " + notification.getCause()))
                .build();

        final List<Thread> threads = IntStream.range(0, 4)
                .mapToObj(k -> new Thread(() -> {
                    for (int i = 0; i < 10; i++) {
                        final int payload = 1;//ThreadLocalRandom.current().nextInt(1, 3);
                        Weightable weightable = null;
                        try {
                            System.out.println("Getting #" + payload + " from " + Thread.currentThread().getName());
                            weightable = cache.get("#" + payload, () -> {
                                        System.out.println("Loading #" + payload + " from " + Thread.currentThread().getName());
                                        Thread.sleep(500);
                                        return new Item(payload, ThreadLocalRandom.current().nextInt(10, 500));
                                    }
                            );
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        }
                        if (weightable != null) {
                            System.out.println("Got " + weightable);
                        }
                    }
                })).collect(Collectors.toList());

        threads.forEach(Thread::start);
        threads.forEach(t -> {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
//        for (int i = 0; i < 200_000; i++) {
//            cache.put("#" + i, new Item(i, ThreadLocalRandom.current().nextInt(10, 500)));
//        }

        System.out.println(cache.size());
    }
}
