package org.cbir.retrieval.web.rest;

import com.codahale.metrics.annotation.Timed;
import org.cbir.retrieval.security.AuthoritiesConstants;
import org.cbir.retrieval.service.RetrievalService;
import org.cbir.retrieval.service.exception.*;
import org.cbir.retrieval.web.rest.dto.StorageJSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
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

    @RequestMapping(value = "/storages/{storage}/images/{id}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    @RolesAllowed(AuthoritiesConstants.USER)
    ResponseEntity<Map> getByStorage(@PathVariable Long id,@PathVariable String storage) throws ResourceNotFoundException {
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
        log.debug("REST request to get image : "+id);

        RetrievalServer retrievalServer = retrievalService.getRetrievalServer();

        List<Map<String,String>> properties =
            retrievalServer.getStorageList()
                .parallelStream()
                .map(x -> x.getProperties(id))
                .filter(x -> x != null && !x.isEmpty())
                .collect(Collectors.toList());

        log.info(properties.toString());

        if(properties.isEmpty())
            throw new ResourceNotFoundException("Image "+ id +" cannot be found !");

        return new ResponseEntity<>(properties.get(0), HttpStatus.OK);
    }

    @RequestMapping(value="/storages/{storage}/images",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    @Timed
    @RolesAllowed(AuthoritiesConstants.USER)
    ResponseEntity<List> getAllByStorage(@PathVariable String storage) throws CBIRException{
        log.debug("REST request to list images : {}");

        RetrievalServer retrievalServer = retrievalService.getRetrievalServer();
        Storage storageImage = retrievalServer.getStorage(storage);
        if(storageImage==null)
            throw new ResourceNotFoundException("Storage "+ storage +" cannot be found!");

        try {
            List<Map<String,String>> map = storageImage.getAllPicturesMap()
                .entrySet()
                .stream()
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
            return new ResponseEntity<>(map, HttpStatus.OK);
        } catch(Exception e) {
            throw new CBIRException(e.toString(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
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

    @RequestMapping(value = "/images",
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
  public ResponseEntity<Map<String,String>> create(
    @RequestParam(value="id", required = false) Long idImage,
    @RequestParam(value="storage",required = false) String idStorage,
    @RequestParam(required = false) String keys,
    @RequestParam(required = false) String values,
    @RequestParam(defaultValue = "false") Boolean async,//http://stackoverflow.com/questions/17693435/how-to-give-default-date-values-in-requestparam-in-spring
    //MultipartHttpServletRequest request
    @RequestParam("file") MultipartFile file
    ) throws CBIRException
    {
        log.debug("REST request to create image : {}", idImage);
        RetrievalServer retrievalServer = retrievalService.getRetrievalServer();

        Storage storage;
        if(idStorage==null) {
            storage = retrievalServer.getNextStorage();
        } else {
            storage = retrievalServer.getStorage(idStorage,true);
        }

        BufferedImage image;
        try {
//            if(request.getFileNames().hasNext())
//                System.out.println(request.getFileNames().next());
//
//            System.out.println("multipart.file="+request.getMultipartHeaders("file"));
//
//            byte[] data = new byte[]{};
//            Map<String, MultipartFile> files = request.getFileMap();
//            for (MultipartFile file : files.values()) {
//                data = file.getBytes();
//            }
            System.out.println(file.getOriginalFilename());
            byte[] data = file.getBytes();
            image = ImageIO.read(new ByteArrayInputStream(data));
        } catch(IOException ex) {
            throw new ResourceNotValidException("Image not valid:"+ex.toString());
        }


        Map<String,String> properties = new TreeMap<>();
        if(keys!=null) {
            String[] keysArray = keys.split(";");
            String[] valuesArray = values.split(";");

            if(keysArray.length!=valuesArray.length) {
                throw new ParamsNotValidException("keys.size()!=values.size()");
            }

            for(int i=0;i<keysArray.length;i++) {
                properties.put(keysArray[i],valuesArray[i]);
            }
        }

        //index picture
        Long id;
        try {
            if (async) {
                id = storage.addToIndexQueue(image, idImage, properties);
            } else {
                id = storage.indexPicture(image, idImage, properties);
            }
        } catch(AlreadyIndexedException e) {
            throw new ResourceAlreadyExistException("Image "+ idImage + "already exist in storage "+idStorage);
        }catch(NoValidPictureException e) {
            throw new ResourceNotValidException("Cannot insert image:"+e.toString());
        }catch(Exception e) {
            throw new CBIRException("Cannot insert image:"+e.toString(),HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(storage.getProperties(id), HttpStatus.OK);
    }

    @RequestMapping(value = "/storages/{storage}/images/{id}",
        method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed
    public void delete(
            @PathVariable Long id,
            @PathVariable String storage
    ) throws Exception {
        log.debug("REST request to delete storage : {}", id);
        RetrievalServer retrievalServer = retrievalService.getRetrievalServer();

        Storage storageImage = retrievalServer.getStorage(storage);
        if(storageImage==null)
            throw new ResourceNotFoundException("Storage "+ storage +" cannot be found!");

        if(!storageImage.isPictureInIndex(id))
            throw new ResourceNotFoundException("Image "+ id +" cannot be found on storage "+storage+" !");

        try {
            storageImage.deletePicture(id);
        } catch(Exception e) {
            log.error(e.getMessage());
            throw new CBIRException("Cannot delete image:"+e.toString(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
