package com.github.ykiselev.aggrid.sources;

import com.github.ykiselev.aggrid.domain.request.AgGridGetRowsRequest;
import com.github.ykiselev.aggrid.domain.response.AgGridGetRowsResponse;

/**
 * @author Yuriy Kiselev (uze@yandex.ru).
 */
public interface AgGridRowSource {

    AgGridGetRowsResponse getRows(AgGridGetRowsRequest request);
}
