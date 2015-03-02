package org.cbir.retrieval.web.rest;

import com.codahale.metrics.annotation.Timed;
import org.cbir.retrieval.security.AuthoritiesConstants;
import org.cbir.retrieval.service.RetrievalService;
import org.cbir.retrieval.service.exception.CBIRException;
import org.cbir.retrieval.service.exception.ResourceNotFoundException;
import org.cbir.retrieval.web.rest.dto.StorageJSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import retrieval.server.RetrievalServer;
import retrieval.storage.Storage;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST controller for managing images.
 */
@RestController
@RequestMapping("/api")
public class ImageResource {

    private final Logger log = LoggerFactory.getLogger(ImageResource.class);

    @Inject
    private RetrievalService retrievalService;

    @RequestMapping(value = "/storages/{id}/images",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @RolesAllowed(AuthoritiesConstants.USER)
    ResponseEntity<Map> getByStorage(@PathVariable Long id,@RequestParam String storage) throws ResourceNotFoundException {
        log.debug("REST request to get image : {}");

        RetrievalServer retrievalServer = retrievalService.getRetrievalServer();

        Storage storageImage = retrievalServer.getStorage(storage);
        if(storageImage==null)
            throw new ResourceNotFoundException("Storage "+ storage +" cannot be found!");

        Map<String,String> properties = storageImage.getProperties(id);
        if(properties==null)
            throw new ResourceNotFoundException("Image "+ id +" cannot be found on storage "+storage+" !");

        return new ResponseEntity<>(properties, HttpStatus.OK);
    }

    @RequestMapping(value = "/images/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @RolesAllowed(AuthoritiesConstants.USER)
    ResponseEntity<Map> get(@PathVariable Long id) throws ResourceNotFoundException {
        log.debug("REST request to get image : {}");

        RetrievalServer retrievalServer = retrievalService.getRetrievalServer();

        Optional<Map<String,String>> properties =
            Optional.of(retrievalServer.getStorageList()
                .parallelStream()
                .map(x -> x.getProperties(id))
                .filter(x -> x != null)
                .reduce((previous, current) -> current)
                .get());

        if(!properties.isPresent())
            throw new ResourceNotFoundException("Image "+ id +" cannot be found !");

        return new ResponseEntity<>(properties.get(), HttpStatus.OK);
    }


    @RequestMapping(value="/images",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Timed
    @RolesAllowed(AuthoritiesConstants.USER)
    ResponseEntity<List> getAll() throws CBIRException{
        log.debug("REST request to list images : {}");

        RetrievalServer retrievalServer = retrievalService.getRetrievalServer();

        try {
            List<Map<String,String>> map = retrievalServer.getInfos()
                .entrySet()
                .stream()
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
            return new ResponseEntity<>(map, HttpStatus.OK);
        } catch(Exception e) {
            throw new CBIRException(e.toString(),HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }


















//    /**
//     * POST -> Create a new storage.
//     */
//    @RequestMapping(value = "/storages",
//        method = RequestMethod.POST,
//        produces = MediaType.APPLICATION_JSON_VALUE)
//    @Timed
//    public void create(@RequestBody StorageJSON storageJSON) throws RessourceAlreadyExistException{
//        log.debug("REST request to save storage : {}", storageJSON.getId());
//        RetrievalServer retrievalServer = retrievalService.getRetrievalServer();
//
//        if(retrievalServer.getStorage(storageJSON.getId())!=null) {
//            throw new RessourceAlreadyExistException("Storage "+ storageJSON.getId() +" already exist!");
//        }
//
//        try {
//            retrievalServer.createStorage(storageJSON.getId());
//        } catch(Exception e) {
//              log.error(e.getMessage());
//        }
//
//    }
//
//    @RequestMapping(value="/storages",
//        method = RequestMethod.GET,
//        produces = MediaType.APPLICATION_JSON_VALUE
//    )
//    @Timed
//    @RolesAllowed(AuthoritiesConstants.USER)
//    List<StorageJSON> getAll() {
//        log.debug("REST request to list storages : {}");
//
//        RetrievalServer retrievalServer = retrievalService.getRetrievalServer();
//
//        List<StorageJSON> storagesJSON =
//            retrievalServer.getStorageList()
//                .stream()
//                .map(StorageJSON::new)
//                .collect(Collectors.toList());
//
//        return storagesJSON;
//    }
//

//
//
//    @RequestMapping(value = "/storages/{id}",
//        method = RequestMethod.DELETE,
//        produces = MediaType.APPLICATION_JSON_VALUE)
//    @Timed
//    public void delete(@PathVariable String id) throws Exception {
//        log.debug("REST request to delete storage : {}", id);
//        RetrievalServer retrievalServer = retrievalService.getRetrievalServer();
//
//        if(retrievalServer.getStorage(id)==null)
//            throw new StorageNotFoundException("Storage "+ id +" cannot be found!");
//
//        try {
//            retrievalServer.deleteStorage(id);
//        } catch(Exception e) {
//            log.error(e.getMessage());
//            throw e;
//        }
//
//    }

}
