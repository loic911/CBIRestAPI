package org.cbir.retrieval.service.exception;

import org.springframework.http.HttpStatus;

/**
 * Created by lrollus on 01/03/15.
 */

public class ResourceAlreadyExistException extends CBIRException {

    public ResourceAlreadyExistException(String message) {
        super(message, HttpStatus.CONFLICT);
    }

}
