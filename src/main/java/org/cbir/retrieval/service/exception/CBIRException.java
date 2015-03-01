package org.cbir.retrieval.service.exception;

/**
 * Created by lrollus on 01/03/15.
 */
public class CBIRException extends Exception {

    private String code;

    public CBIRException(String message, String code) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return this.code;
    }
}
