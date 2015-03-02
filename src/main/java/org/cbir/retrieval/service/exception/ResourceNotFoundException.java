package org.cbir.retrieval.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by lrollus on 01/03/15.
 */

public class ResourceNotFoundException extends CBIRException {

    public ResourceNotFoundException(String message) {
        super(message,HttpStatus.NOT_FOUND);
    }

}
