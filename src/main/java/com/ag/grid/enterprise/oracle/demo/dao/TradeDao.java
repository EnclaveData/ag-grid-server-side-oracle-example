package com.ag.grid.enterprise.oracle.demo.dao;

import com.ag.grid.enterprise.oracle.demo.request.AgGridGetRowsRequest;
import com.ag.grid.enterprise.oracle.demo.response.AgGridGetRowsResponse;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public interface TradeDao {

    AgGridGetRowsResponse getData(AgGridGetRowsRequest request);
}
