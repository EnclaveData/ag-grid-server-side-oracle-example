package com.github.ykiselev.ag.grid.data;

import com.github.ykiselev.ag.grid.api.request.AgGridGetRowsRequest;
import com.github.ykiselev.ag.grid.api.response.AgGridGetRowsResponse;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public interface AgGridRowSource {

    AgGridGetRowsResponse getRows(AgGridGetRowsRequest request);
}
