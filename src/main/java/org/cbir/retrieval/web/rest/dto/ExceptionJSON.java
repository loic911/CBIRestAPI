package org.cbir.retrieval.web.rest.dto;

import org.cbir.retrieval.service.exception.CBIRException;

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

    public String getCode() {
        return exception.getCode();
    }

}
