package com.ag.grid.enterprise.oracle.demo.dao;

import com.github.ykiselev.ag.grid.api.request.AgGridGetRowsRequest;
import com.github.ykiselev.ag.grid.api.response.AgGridGetRowsResponse;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public interface TradeDao {

    AgGridGetRowsResponse getData(AgGridGetRowsRequest request);
}
