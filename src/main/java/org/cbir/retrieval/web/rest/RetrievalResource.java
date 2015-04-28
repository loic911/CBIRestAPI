package org.cbir.retrieval.web.rest;
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
import com.codahale.metrics.annotation.Timed;
import org.cbir.retrieval.security.AuthoritiesConstants;
import org.cbir.retrieval.service.RetrievalService;
import org.cbir.retrieval.web.rest.dto.RetrievalServerJSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import java.util.Optional;

/**
 * REST controller for managing users.
 */
@RestController
@RequestMapping("/api")
public class RetrievalResource {

    private final Logger log = LoggerFactory.getLogger(RetrievalResource.class);

    @Inject
    private RetrievalService retrievalService;



    @RequestMapping(value = "/server",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @RolesAllowed(AuthoritiesConstants.ADMIN)
    ResponseEntity<RetrievalServerJSON> getServer() {
        log.debug("REST request to get server : {}");
        return Optional.ofNullable(retrievalService.getRetrievalServer())
            .map(server -> new ResponseEntity<>(new RetrievalServerJSON(server), HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }



}
