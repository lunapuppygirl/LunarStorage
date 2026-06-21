package dev.lunapuppygirl.lunarstorage.web.filters;

import dev.lunapuppygirl.lunarstorage.managers.StatsManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RequestFilter extends OncePerRequestFilter {
    private final StatsManager statsManager;

    public RequestFilter(StatsManager statsManager) {
        this.statsManager = statsManager;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        statsManager.logRequest();

        filterChain.doFilter(request, response);
    }
}
