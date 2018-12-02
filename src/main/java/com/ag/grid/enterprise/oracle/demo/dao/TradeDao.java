package com.ag.grid.enterprise.oracle.demo.dao;

import com.ag.grid.enterprise.oracle.demo.request.EnterpriseGetRowsRequest;
import com.ag.grid.enterprise.oracle.demo.response.EnterpriseGetRowsResponse;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public interface TradeDao {

    EnterpriseGetRowsResponse getData(EnterpriseGetRowsRequest request);
}
