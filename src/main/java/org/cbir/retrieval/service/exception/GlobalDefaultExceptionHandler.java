package org.cbir.retrieval.service.exception;

import org.cbir.retrieval.web.rest.dto.ExceptionJSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;

/*
 * Copyright (c) 2009-2015. Authors: see NOTICE file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@ControllerAdvice
class GlobalDefaultExceptionHandler {
    public static final String DEFAULT_ERROR_VIEW = "error";

    private final Logger log = LoggerFactory.getLogger(GlobalDefaultExceptionHandler.class);

    @ExceptionHandler(value = CBIRException.class)
    public ResponseEntity<ExceptionJSON> defaultErrorHandler(HttpServletRequest req, Exception e) throws Exception {
        log.error(e.toString());
        if(e instanceof CBIRException) {
            return new ResponseEntity<ExceptionJSON>(new ExceptionJSON((CBIRException)e), ((CBIRException) e).getStatus());
        } else {
            throw e;
        }

    }
}
