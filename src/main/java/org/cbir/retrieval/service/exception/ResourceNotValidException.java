package org.cbir.retrieval.service.exception;

import org.springframework.http.HttpStatus;

/**
 * Created by lrollus on 01/03/15.
 */

public class ResourceNotValidException extends CBIRException {

    public ResourceNotValidException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }

}
