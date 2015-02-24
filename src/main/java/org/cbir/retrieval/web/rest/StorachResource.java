package org.cbir.retrieval.web.rest;

import com.codahale.metrics.annotation.Timed;
import org.cbir.retrieval.domain.Storach;
import org.cbir.retrieval.repository.StorachRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing Storach.
 */
@RestController
@RequestMapping("/app")
public class StorachResource {

    private final Logger log = LoggerFactory.getLogger(StorachResource.class);

    @Inject
    private StorachRepository storachRepository;

    /**
     * POST  /rest/storachs -> Create a new storach.
     */
    @RequestMapping(value = "/rest/storachs",
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public void create(@RequestBody Storach storach) {
        log.debug("REST request to save Storach : {}", storach);
        storachRepository.save(storach);
    }

    /**
     * GET  /rest/storachs -> get all the storachs.
     */
    @RequestMapping(value = "/rest/storachs",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public List<Storach> getAll() {
        log.debug("REST request to get all Storachs");
        return storachRepository.findAll();
    }

    /**
     * GET  /rest/storachs/:id -> get the "id" storach.
     */
    @RequestMapping(value = "/rest/storachs/{id}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public ResponseEntity<Storach> get(@PathVariable Long id) {
        log.debug("REST request to get Storach : {}", id);
        return Optional.ofNullable(storachRepository.findOne(id))
            .map(storach -> new ResponseEntity<>(
                storach,
                HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * DELETE  /rest/storachs/:id -> delete the "id" storach.
     */
    @RequestMapping(value = "/rest/storachs/{id}",
            method = RequestMethod.DELETE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public void delete(@PathVariable Long id) {
        log.debug("REST request to delete Storach : {}", id);
        storachRepository.delete(id);
    }
}
