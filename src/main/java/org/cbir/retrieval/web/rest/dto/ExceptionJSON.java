package org.cbir.retrieval.web.rest.dto;

import org.cbir.retrieval.service.exception.CBIRException;
import org.springframework.http.HttpStatus;
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
