package com.hieunn.mvel.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
public class LoggingFilter extends OncePerRequestFilter implements Ordered {
    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        long start = System.nanoTime();

        String timestamp = LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")
        );
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String query = request.getQueryString();
        String fullApi = uri + (query != null ? ("?" + query) : "");

        log.info(
                "[REQUEST] {} {} | timestamp = {}",
                method,
                fullApi,
                timestamp
        );

        filterChain.doFilter(request, response);

        long duration = (System.nanoTime() - start) / 1_000_000;

        log.info(
                "[RESPONSE] {} {} | status = {} | duration = {}ms | timestamp = {}",
                method,
                fullApi,
                response.getStatus(),
                duration,
                timestamp
        );
    }
}
