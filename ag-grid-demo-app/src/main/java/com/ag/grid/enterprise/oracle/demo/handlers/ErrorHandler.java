package com.ag.grid.enterprise.oracle.demo.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Yuriy Kiselev (uze@yandex.ru)
 * @since 24.12.2018
 */
@Component
public final class ErrorHandler extends AbstractHandlerExceptionResolver {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    protected ModelAndView doResolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        logger.error("Unhandled exception!", ex);
        try {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.toString());
        } catch (Exception ex2) {
            logger.error("Unable to send error to the client!", ex2);
        }
        return null;
    }
}
