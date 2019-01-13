package com.ag.grid.enterprise.oracle.demo.dao;

import com.github.ykiselev.ag.grid.api.filter.ColumnFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * @author Yuriy Kiselev (uze@yandex.ru)
 * @since 13.01.2019
 */
public final class DeferredColumnIndex<K, C> implements ColumnIndex<K, C> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final String column;

    private final Function<C, Collection<K>> toKeys;

    private final Future<ColumnIndex<K, C>> future;

    private final Object lock = new Object();

    private volatile ColumnIndex<K, C> delegate;

    public DeferredColumnIndex(String column, Function<C, Collection<K>> toKeys, Future<ColumnIndex<K, C>> future) {
        this.column = requireNonNull(column);
        this.toKeys = requireNonNull(toKeys);
        this.future = requireNonNull(future);
    }

    @Override
    public String getColumn() {
        return column;
    }

    @Override
    public Collection<K> getKeys(C container, ColumnFilter filter) {
        for (; ; ) {
            final ColumnIndex<K, C> delegate = this.delegate;
            if (delegate != null) {
                return delegate.getKeys(container, filter);
            }
            if (!future.isDone()) {
                break;
            }
            synchronized (lock) {
                if (this.delegate == null) {
                    try {
                        this.delegate = future.get();
                    } catch (InterruptedException e) {
                        logger.warn("Indexing was interrupted!", e);
                        this.delegate = new NoopColumnIndex<>(column, toKeys);
                    } catch (ExecutionException e) {
                        logger.error("Indexing has failed!", e);
                        this.delegate = new NoopColumnIndex<>(column, toKeys);
                    }
                }
            }
        }
        return toKeys.apply(container);
    }

    @Override
    public String toString() {
        return "DeferredColumnIndex{" +
                "column='" + column + '\'' +
                ", delegate=" + delegate +
                '}';
    }
}
