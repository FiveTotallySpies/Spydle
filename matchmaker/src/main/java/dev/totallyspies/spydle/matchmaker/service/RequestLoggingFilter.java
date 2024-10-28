package dev.totallyspies.spydle.matchmaker.service;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.util.Enumeration;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {

        // Wrap the request to cache the request body
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);

        // Continue the filter chain
        filterChain.doFilter(wrappedRequest, response);

        // Log the request headers and body after the request has been processed
        logRequestDetails(wrappedRequest);
    }

    private void logRequestDetails(ContentCachingRequestWrapper request) throws IOException {
        // Log headers
        System.out.println("Request Headers:");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            System.out.println(headerName + ": " + request.getHeader(headerName));
        }

        // Log body
        byte[] body = request.getContentAsByteArray();
        if (body.length > 0) {
            String bodyString = new String(body, request.getCharacterEncoding());
            System.out.println("Request Body: " + bodyString);
        }
    }
}