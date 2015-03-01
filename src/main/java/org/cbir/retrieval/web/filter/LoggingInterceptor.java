package org.cbir.retrieval.web.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;

/**
 * Created by lrollus on 01/03/15.
 */
public class LoggingInterceptor implements WebRequestInterceptor {

    private final Logger log = LoggerFactory.getLogger(LoggingInterceptor.class);

    @Override
    public void preHandle(WebRequest request) throws Exception {
        log.debug("preHandle: session="+request.getSessionId());
    }

    @Override
    public void postHandle(WebRequest request, ModelMap model) throws Exception {

    }

    @Override
    public void afterCompletion(WebRequest request, Exception ex) throws Exception {

    }
}
