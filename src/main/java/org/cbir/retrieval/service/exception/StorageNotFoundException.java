package org.cbir.retrieval.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Created by lrollus on 01/03/15.
 */

public class StorageNotFoundException extends CBIRException {

    public StorageNotFoundException(String message) {
        super(message,HttpStatus.NOT_FOUND);
    }

}
