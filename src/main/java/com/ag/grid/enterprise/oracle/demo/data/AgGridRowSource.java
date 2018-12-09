package com.ag.grid.enterprise.oracle.demo.data;

import com.ag.grid.enterprise.oracle.demo.request.AgGridGetRowsRequest;
import com.ag.grid.enterprise.oracle.demo.response.AgGridGetRowsResponse;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public interface AgGridRowSource {

    AgGridGetRowsResponse getRows(AgGridGetRowsRequest request);
}
