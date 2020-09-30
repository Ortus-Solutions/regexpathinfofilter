package org.cfmlprojects.regexpathinfofilter;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;


public class RegexPathInfoFilter implements Filter {

    private static Pattern regex;
    private static final String REGEX_DEFAULT = "^(/.+\\.cf[cm])(/.*)";
    private static final Logger logger = Logger.getLogger(RegexPathInfoFilter.class.getName());

    @Override
    public void init(FilterConfig config) throws ServletException {
        final String regexString =
            config.getInitParameter("regex") != null ? config.getInitParameter("regex") : REGEX_DEFAULT;
        regex = Pattern.compile(regexString);
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res,
                         FilterChain chain) throws ServletException, IOException {

        final String requestURI = ((HttpServletRequest) req).getRequestURI();

        final Matcher matcher = regex.matcher(requestURI);
        if (matcher.matches()) {
            final String contextPath = ((HttpServletRequest) req).getContextPath();
            final String newURL = matcher.group(1).replaceFirst(contextPath, "");
            final String path_info = matcher.group(2);

            logger.log(Level.FINE, requestURI + " matches " + regex + " regex, now: " + newURL + " path_info: "
                                   + path_info);

            final HttpServletRequestWrapper wrapped = new HttpServletRequestWrapper((HttpServletRequest) req) {
                @Override
                public String getPathInfo() {
                    return path_info;
                }
            };
            req.getRequestDispatcher(newURL).forward(wrapped, res);
        } else {
            chain.doFilter(req, res);
        }
    }

    @Override
    public void destroy() {
        //
    }
}
