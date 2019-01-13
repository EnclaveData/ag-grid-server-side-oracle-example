package com.ag.grid.enterprise.oracle.demo.dao;

import com.ag.grid.enterprise.oracle.demo.domain.Portfolio;
import com.ag.grid.enterprise.oracle.demo.domain.Trade;
import com.github.ykiselev.ag.grid.data.RequestFilters;
import com.github.ykiselev.ag.grid.data.types.TypeInfo;
import com.tangosol.net.NamedCache;

import java.util.Collection;

/**
 * @author Yuriy Kiselev (uze@yandex.ru)
 * @since 13.01.2019
 */
public interface PortfolioKeyCache {

    Collection<Long> getKeys(Portfolio portfolio, RequestFilters filters, TypeInfo<Trade> typeInfo, NamedCache<Long, Trade> trades);

    String getInfo();
}
