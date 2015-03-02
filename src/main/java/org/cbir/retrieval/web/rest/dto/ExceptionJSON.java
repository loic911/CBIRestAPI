package org.cbir.retrieval.web.rest.dto;

import org.cbir.retrieval.service.exception.CBIRException;
import org.springframework.http.HttpStatus;

/**
 * Created by lrollus on 01/03/15.
 */
public class ExceptionJSON {

    private CBIRException exception;

    public ExceptionJSON(CBIRException exception) {
        this.exception = exception;
    }

    public String getMessage() {
        return exception.getMessage();
    }

    public int getCode() {
        return exception.getStatus().value();
    }

    public HttpStatus getStatus() {
        return exception.getStatus();
    }
}
