package org.cbir.retrieval.service.exception;

import org.springframework.http.HttpStatus;

/**
 * Created by lrollus on 01/03/15.
 */
public class CBIRException extends Exception {

    private HttpStatus status;

    public CBIRException(String message, HttpStatus code) {
        super(message);
        this.status = code;
    }

    public HttpStatus getStatus() {
        return this.status;
    }
}
