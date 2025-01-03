package net.risesoft.config;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CheckLoginFilter implements Filter {
    protected final Logger log = LoggerFactory.getLogger(CheckLoginFilter.class);

    @Override
    public void destroy() {
    }

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        chain.doFilter(servletRequest, servletResponse);
    }

}
