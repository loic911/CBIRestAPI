package org.cbir.retrieval.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cbir.retrieval.domain.User;
import org.cbir.retrieval.repository.UserRepository;
import org.cbir.retrieval.security.AuthoritiesConstants;
import org.cbir.retrieval.service.RetrievalService;
import org.cbir.retrieval.web.rest.dto.RetrievalServerJSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import retrieval.server.RetrievalServer;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

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
