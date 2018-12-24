package com.ag.grid.enterprise.oracle.demo.filters;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author Yuriy Kiselev (uze@yandex.ru)
 * @since 24.12.2018
 */
@Component
@Order(1)
public class TimingFilter implements Filter {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // no-op
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        final long t0 = System.currentTimeMillis();
        chain.doFilter(request, response);
        final long t1 = System.currentTimeMillis();
        final CharSequence uri;
        if (request instanceof HttpServletRequest) {
            final HttpServletRequest httpRequest = (HttpServletRequest) request;
            final StringBuffer sb = httpRequest.getRequestURL();
            if (!StringUtils.isEmpty(httpRequest.getQueryString())) {
                sb.append('?').append(httpRequest.getQueryString());
            }
            uri = sb;
        } else {
            uri = "non-http URI";
        }
        logger.info("Request to {} processed in {} ms", uri, t1 - t0);
    }

    @Override
    public void destroy() {
        // no-op
    }
}
