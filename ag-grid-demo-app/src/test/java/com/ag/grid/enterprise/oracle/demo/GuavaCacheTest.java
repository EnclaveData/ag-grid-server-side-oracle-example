package com.ag.grid.enterprise.oracle.demo;

import com.google.common.base.Ticker;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.Weigher;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Objects.requireNonNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

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

    static final class MyKey1 {

        private final String id;

        public String id() {
            return id;
        }

        MyKey1(String id) {
            this.id = requireNonNull(id);
        }

        @Override
        public String toString() {
            return "MyKey1{" +
                    "id='" + id + '\'' +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MyKey1 myKey1 = (MyKey1) o;
            return id.equals(myKey1.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }

    @Test
    @Ignore
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

    /*
        static final class MyCache<K, V> implements AutoCloseable {

            private final Map<K, CompletableFuture<V>> map = new ConcurrentHashMap<>();

            private final ExecutorService executor = Executors.newCachedThreadPool();

            private final Cache<K, V> cache;

            MyCache(Cache<K, V> cache) {
                this.cache = requireNonNull(cache);
            }

            private V load(K key) {
                return submit(key).join();
            }

            public V get(K key) {
                try {
                    return cache.get(key, () -> load(key));
                } catch (ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }

            private CompletableFuture<V> submit(K key) {
                final CompletableFuture<V> future = map.compute(key, (k, prev) -> CompletableFuture.supplyAsync(k::load, executor));
                future.thenAccept(result -> map.remove(key, future));
                return future;
            }

            private void invalidate(K key) {
                submit(key).thenAccept(v -> cache.put(key, v));
            }

            public void invalidate(Predicate<K> predicate) {
                cache.asMap().keySet().stream().filter(predicate).forEach(this::invalidate);
            }

            @Override
            public void close() {
                executor.shutdown();
            }
        }
    */

    @Test(timeout = 500)
    public void shouldInvalidate() throws ExecutionException {
        final MyTicker ticker = new MyTicker();
        final ExecutorService executor = Executors.newCachedThreadPool();
        final Map<Class<?>, Function> loaders = ImmutableMap.<Class<?>, Function>builder()
                .put(MyKey1.class, key -> {
                    System.out.println(Thread.currentThread().getName() + " : Loading value for key " + key);
                    return new Item(123, 5);
                })
                .build();
        final LoadingCache<Object, Weightable> cache = CacheBuilder.newBuilder()
                .expireAfterAccess(5, TimeUnit.SECONDS)
                .maximumWeight(10)
                .ticker(ticker)
                .weigher((Weigher<Object, Weightable>) (key, value) -> value.getWeight())
                .build(new CacheLoader<Object, Weightable>() {
                    @Override
                    public Weightable load(Object key) {
                        return doLoad(key);
                    }

                    @Override
                    public ListenableFuture<Weightable> reload(Object key, Weightable oldValue) {
                        return Futures.submitAsync(() -> Futures.immediateFuture(doLoad(key)), executor);
                    }

                    private Weightable doLoad(Object key) {
                        requireNonNull(key, "Key can't be null!");
                        @SuppressWarnings("unchecked") final Function<Object, Weightable> loader = loaders.get(key.getClass());
                        if (loader != null) {
                            return loader.apply(key);
                        }
                        throw new IllegalArgumentException("Unsupported key: " + key);
                    }
                });

        final Weightable a = cache.get(new MyKey1("a"));
        assertNotNull(a);

        Weightable a1 = cache.getIfPresent(new MyKey1("a"));
        assertSame(a, a1);

        cache.refresh(new MyKey1("a"));

        while (cache.get(new MyKey1("a")) == a) {
            // no-op
        }
        assertNotSame(a, cache.getIfPresent(new MyKey1("a")));

        ticker.value = TimeUnit.SECONDS.toNanos(5);

        final Weightable a3 = cache.get(new MyKey1("a"));
        assertNotSame(a, a3);

        ticker.value = TimeUnit.SECONDS.toNanos(12);

        Weightable a4 = cache.getIfPresent(new MyKey1("a"));
        assertNull(a4);
    }
}

final class MyTicker extends Ticker {

    public volatile long value;

    @Override
    public long read() {
        return value;
    }
}