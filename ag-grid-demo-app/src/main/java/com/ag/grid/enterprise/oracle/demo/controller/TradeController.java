package com.ag.grid.enterprise.oracle.demo.controller;

import com.ag.grid.enterprise.oracle.demo.dao.TradeDao;
import com.github.ykiselev.ag.grid.api.request.AgGridGetRowsRequest;
import com.github.ykiselev.ag.grid.api.response.AgGridGetRowsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
public class TradeController {

    private final TradeDao tradeDao;

    /**
     * @param tradeDao any descendant of {@link TradeDao}
     */
    @Autowired
    public TradeController(@Qualifier("databaseTradeDao") TradeDao tradeDao) {
        this.tradeDao = tradeDao;
    }

    @RequestMapping(method = POST, value = "/getRows")
    @ResponseBody
    public AgGridGetRowsResponse getRows(@RequestBody AgGridGetRowsRequest request,
                                         @RequestParam(name = "portfolio", required = false) String portfolio,
                                         HttpSession session
    ) {
        return tradeDao.getData(request);
    }

    @GetMapping("/cache")
    public String getCacheInfo() {
        return tradeDao.getCacheInfo();
    }
}