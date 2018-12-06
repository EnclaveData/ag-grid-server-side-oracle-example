package com.ag.grid.enterprise.oracle.demo.controller;

import com.ag.grid.enterprise.oracle.demo.dao.TradeDao;
import com.ag.grid.enterprise.oracle.demo.request.EnterpriseGetRowsRequest;
import com.ag.grid.enterprise.oracle.demo.response.EnterpriseGetRowsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;

import java.util.concurrent.TimeUnit;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
public class TradeController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final TradeDao tradeDao;

    @Autowired
    public TradeController(@Qualifier("cacheBasedTradeDao") TradeDao tradeDao) {
        this.tradeDao = tradeDao;
    }

    @RequestMapping(method = POST, value = "/getRows")
    @ResponseBody
    public EnterpriseGetRowsResponse getRows(@RequestBody EnterpriseGetRowsRequest request) {
        final long t0 = System.nanoTime();
        try {
            return tradeDao.getData(request);
        } finally {
            logger.info("getRows() complete in {} ms", TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - t0));
        }
    }

    @ExceptionHandler({Throwable.class})
    protected ResponseEntity<Object> handleInvalidRequest(RuntimeException e, WebRequest request) {
        logger.error("Unhandled exception!", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(e.toString());
    }
}