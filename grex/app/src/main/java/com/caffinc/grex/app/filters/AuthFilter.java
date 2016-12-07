package com.caffinc.grex.app.filters;

import com.caffinc.grex.app.utils.AuthToken;
import com.caffinc.grex.core.Load;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.eclipse.jetty.http.HttpStatus;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Filters requests to Grex
 *
 * @author Sriram
 */
public class AuthFilter implements Filter {
    private static final Logger LOG = Logger.getLogger(AuthFilter.class);
    private static final Set<String> EXCLUDE_GET = new HashSet<>(
            Arrays.asList("/swagger.json", "/favicon.ico", "/docs", "/docs/"));

    private boolean doAuth = false;


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (doAuth) {
            HttpServletRequest req = (HttpServletRequest) request;
            LOG.debug(req.getMethod() + " " + req.getRequestURI());

            if ("GET".equals(req.getMethod())) {
                String requestEndpoint = req.getRequestURI();
                requestEndpoint = requestEndpoint.substring(requestEndpoint.lastIndexOf("/"));
                if (EXCLUDE_GET.contains(requestEndpoint.toLowerCase())) {
                    chain.doFilter(request, response);
                    return;
                }
            } else if ("OPTIONS".equals(req.getMethod())) {
                chain.doFilter(request, response);
                return;
            }

            // Get the authentication passed in HTTP headers parameters
            String auth = req.getHeader("authorization");
            if (auth == null) {
                ((HttpServletResponse) response).sendError(HttpStatus.UNAUTHORIZED_401, "No authorization header");
                return;
            }

            if (auth.startsWith("Basic") || auth.startsWith("basic")) {
                String authId = new String(Base64.decodeBase64(auth.replaceFirst("[Bb]asic ", "").trim()), "UTF-8");
                if (AuthToken.getInstance().getAuthToken().equals(authId)) {
                    chain.doFilter(request, response);
                    return;
                }
            }
            ((HttpServletResponse) response).sendError(HttpStatus.UNAUTHORIZED_401, "Unauthorized");
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {
        doAuth = true;
    }


    @Override
    public void destroy() {
        // Do nothing
    }
}

