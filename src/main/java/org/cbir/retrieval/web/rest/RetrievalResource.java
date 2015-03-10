package org.cbir.retrieval.web.rest;

import com.codahale.metrics.annotation.Timed;
import org.cbir.retrieval.security.AuthoritiesConstants;
import org.cbir.retrieval.service.RetrievalService;
import org.cbir.retrieval.service.exception.CBIRException;
import org.cbir.retrieval.service.exception.ParamsNotValidException;
import org.cbir.retrieval.service.exception.ResourceAlreadyExistException;
import org.cbir.retrieval.service.exception.ResourceNotValidException;
import org.cbir.retrieval.web.rest.dto.ResultsJSON;
import org.cbir.retrieval.web.rest.dto.RetrievalServerJSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import retrieval.client.RetrievalClient;
import retrieval.dist.ResultsSimilarities;
import retrieval.server.RetrievalServer;
import retrieval.storage.Storage;
import retrieval.storage.exception.AlreadyIndexedException;
import retrieval.storage.exception.NoValidPictureException;

import javax.annotation.security.RolesAllowed;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
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
