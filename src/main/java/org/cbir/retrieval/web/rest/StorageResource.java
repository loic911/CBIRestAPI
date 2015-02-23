package org.cbir.retrieval.web.rest;

import com.codahale.metrics.annotation.Timed;
import org.cbir.retrieval.security.AuthoritiesConstants;
import org.cbir.retrieval.service.RetrievalService;
import org.cbir.retrieval.web.rest.dto.RetrievalServerJSON;
import org.cbir.retrieval.web.rest.dto.StorageJSON;
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
import retrieval.storage.Storage;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST controller for managing users.
 */
@RestController
@RequestMapping("/api")
public class StorageResource {

    private final Logger log = LoggerFactory.getLogger(StorageResource.class);

    @Inject
    private RetrievalService retrievalService;

    @RequestMapping(
        value = "/storage/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @RolesAllowed(AuthoritiesConstants.USER)
    ResponseEntity<StorageJSON> getStorage(@PathVariable String id) {
        log.debug("REST request to get storage : {}");

        RetrievalServer retrievalServer = retrievalService.getRetrievalServer();
        Storage storage = retrievalServer.getStorage(id);

        return Optional.ofNullable(storage)
            .map(itStorage -> new ResponseEntity<>(new StorageJSON(itStorage), HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @RequestMapping(
        value="/storage",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Timed
    @RolesAllowed(AuthoritiesConstants.USER)
    List<StorageJSON> listStorages() {
        log.debug("REST request to list storages : {}");

        RetrievalServer retrievalServer = retrievalService.getRetrievalServer();

        List<StorageJSON> storagesJSON =
            retrievalServer.getStorageList()
            .stream()
            .map(StorageJSON::new)
            .collect(Collectors.toList());

        return storagesJSON;
    }

}
