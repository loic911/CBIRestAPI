package org.cbir.retrieval.web.rest;

import org.cbir.retrieval.Application;
import org.cbir.retrieval.service.RetrievalService;
import org.cbir.retrieval.service.exception.ResourceAlreadyExistException;
import org.cbir.retrieval.service.exception.ResourceNotFoundException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.NestedServletException;
import retrieval.server.RetrievalServer;
import retrieval.storage.Storage;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the UserResource REST controller.
 *
 * @see UserResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class ImageResourceTest {

    public static String DEFAULT_STORAGE = "default";

    public static long NUMBER_OF_PICTURES_AT_BEGINNING = 4;

    public static String[] IMAGE_PATHS = {
        "testdata/images/crop1.jpg",
        "testdata/images/crop2.jpg",
        "testdata/images/crop3.jpg",
        "testdata/images/crop4.jpg",
        "testdata/images/crop5.jpg",
        "testdata/images/crop6.jpg",
        "testdata/images/crop7.jpg",
        "testdata/images/crop8.jpg"
    };

    @Inject
    private RetrievalService retrievalService;

    private RetrievalServer retrievalServer;

    private MockMvc restStorageMockMvc;


    @Before
    public void setup() throws Exception{
        ImageResource imageResource = new ImageResource();
        ReflectionTestUtils.setField(imageResource, "retrievalService", retrievalService);
        this.restStorageMockMvc = MockMvcBuilders.standaloneSetup(imageResource).build();

        retrievalService.reset();
        this.retrievalServer = retrievalService.getRetrievalServer();

        for(int i =1; i<5;i++) {

            Long id = (long)i;
            BufferedImage img = ImageIO.read(new File(IMAGE_PATHS[i-1]));
            Map<String,String> properties = new TreeMap<>();
            properties.put("path",IMAGE_PATHS[i-1]);
            properties.put("date",new Date().toString());

            retrievalServer
                .getStorage(DEFAULT_STORAGE)
                .indexPicture(img, id,properties);
        }

    }

    @Test
    public void testGetImage() throws Exception {

        restStorageMockMvc.perform(get("/api/images/{id}",1L).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.id").value("1"));
    }

    @Test
    public void testGetImageNotExists() throws Exception {

        try {
            restStorageMockMvc.perform(get("/api/images/{id}",0L).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        } catch(NestedServletException e) {
            assertThat(e.getCause().getClass()).isEqualTo(ResourceNotFoundException.class);
        }
    }

    @Test
    public void testGetImageByStorage() throws Exception {

        restStorageMockMvc.perform(get("/api/storages/{storage}/images/{id}",DEFAULT_STORAGE,1L)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.id").value("1"));
    }

    @Test
    public void testGetImageByStorageNotExists() throws Exception {

        try {
            restStorageMockMvc.perform(get("/api/storages/{storage}/images/{id}","unknown",0L).accept(MediaType.APPLICATION_JSON))
                .andReturn();
            assert false;
        } catch(NestedServletException e) {
            assertThat(e.getCause().getClass()).isEqualTo(ResourceNotFoundException.class);
        }
    }

    @Test
    public void testGetAllimages() throws Exception {
        restStorageMockMvc.perform(get("/api/images").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$[0].id").value("1"))
            .andExpect(jsonPath("$[1].id").value("2"))
            .andExpect(jsonPath("$[2].id").value("3"))
            .andExpect(jsonPath("$[3].id").value("4"));
    }

    @Test
    public void testGetAllimagesByStorage() throws Exception {
        restStorageMockMvc.perform(get("/api/storages/{storage}/images",DEFAULT_STORAGE).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$[0].id").value("1"))
            .andExpect(jsonPath("$[1].id").value("2"))
            .andExpect(jsonPath("$[2].id").value("3"))
            .andExpect(jsonPath("$[3].id").value("4"));
    }

    @Test
    public void testGetAllimagesByStorageNotExist() throws Exception {
        try {
            restStorageMockMvc.perform(get("/api/storages/{storage}/images","unknown").accept(MediaType.APPLICATION_JSON))
                .andReturn();
            assert false;
        } catch(NestedServletException e) {
            assertThat(e.getCause().getClass()).isEqualTo(ResourceNotFoundException.class);
        }
    }


    @Test
    public void testAddImage() throws Exception {
        // Validate the database is empty (only default storage)
        assertThat(retrievalServer.getSize()).isEqualTo(NUMBER_OF_PICTURES_AT_BEGINNING);

        File file = new File(IMAGE_PATHS[4]);
        MockMultipartFile firstFile = new MockMultipartFile("file", file.getName(), "image/png", Files.readAllBytes(Paths.get(IMAGE_PATHS[4])));

        MockMultipartFile file1 = new MockMultipartFile(file.getName(), Files.readAllBytes(Paths.get(IMAGE_PATHS[4])));
        MvcResult result = restStorageMockMvc.perform(
            fileUpload("/api/images")
                .file(file1)
                .param("id", "5")
                .param("storage", DEFAULT_STORAGE)
                .param("keys", "date;test")
                .param("values", "2015;test")
                .param("async", "false")
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("5"))
            .andExpect(jsonPath("$.test").value("test"))
            .andReturn();

    }

//        System.out.println(result.getResolvedException().getMessage());
//        System.out.println(result.getResolvedException().toString());
//
//        assertThat(result.getResponse().getStatus()).isEqualTo(200);
//        assertThat(result.getResponse().getStatus()).isEqualTo(200);
//
//
//        Storage storage = retrievalServer.getStorage(DEFAULT_STORAGE);
//        assertThat(storage.getNumberOfItem()).isEqualTo((NUMBER_OF_PICTURES_AT_BEGINNING + 1));
//        assertThat(storage.getProperties(5l)).containsEntry("date","2015");
//        assertThat(storage.getProperties(5l)).containsEntry("test","test");


//    @RequestMapping(value = "/images",
//        method = RequestMethod.POST,
//        produces = MediaType.APPLICATION_JSON_VALUE)
//    @Timed
//    public ResponseEntity<Map<String,String>> create(
//        @RequestParam(value="id") Long idImage,
//        @RequestParam(value="storage") String idStorage,
//        @RequestParam String keys,
//        @RequestParam String values,
//        @RequestParam(defaultValue = "false") Boolean async,//http://stackoverflow.com/questions/17693435/how-to-give-default-date-values-in-requestparam-in-spring
//        MultipartFile imageBytes
//    ) throws CBIRException
//    {
//
//
//
//
//
//
//
//    @Test
//    @Transactional
//    public void createStorage() throws Exception {
//        // Validate the database is empty (only default storage)
//        assertThat(retrievalServer.getStorageList()).hasSize(NUMBER_OF_STORAGE_AT_BOOTSTRAT);
//        String name = "NEW_NAME";
//
//        // Create the Storage
//        MvcResult result = restStorageMockMvc.perform(post("/api/storages")
//            .contentType(TestUtil.APPLICATION_JSON_UTF8)
//            .content("{\"id\":\""+name+"\"}")).andReturn();
//
////        System.out.println(result.getResolvedException().getMessage());
////        System.out.println(result.getResolvedException().toString());
//
//        assertThat(result.getResponse().getStatus()).isEqualTo(200);
//        //System.out.println(result.andReturn().getResponse().getContentAsString());
//
//        // Validate the Storach in the database
//        List<Storage> storages = retrievalServer.getStorageList();
//        assertThat(storages).hasSize(NUMBER_OF_STORAGE_AT_BOOTSTRAT +1);
//        Storage storage = retrievalServer.getStorage(name);
//        assertThat(storage.getStorageName()).isEqualTo(name);
//        assertThat(storage.getNumberOfItem()).isEqualTo(0);
//    }
//
//    @Test
//    @Transactional
//    public void createStorageAlreadyExist() throws Exception {
//        // Validate the database is empty (only default storage)
//        String name = retrievalServer.getStorageList().get(0).getStorageName();
//        int status=-1;
//        // Create the Storage
//        System.out.println("***********************");
//        try {
//            MvcResult result = restStorageMockMvc.perform(post("/api/storages")
//                .contentType(TestUtil.APPLICATION_JSON_UTF8)
//                .content(String.format("{\"id\":\"%s\"}", name))).andReturn();
//            assert false;
//        } catch(NestedServletException e) {
//            assertThat(e.getCause().getClass()).isEqualTo(ResourceAlreadyExistException.class);
//        }
//    }
//
//
//
//
//
//
//    @Test
//    public void testGetStorageUnknown() throws Exception {
//        try {
//            restStorageMockMvc.perform(get("/api/storages/unknown")
//                .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isNotFound());
//            assert false;
//        } catch(NestedServletException e) {
//            assertThat(e.getCause().getClass()).isEqualTo(ResourceNotFoundException.class);
//        }
//    }
//
//    @Test
//    @Transactional
//    public void deleteStorage() throws Exception {
//        retrievalServer.createStorage(TO_DELETE_NAME);
//
//        assertThat(retrievalServer.getStorage(TO_DELETE_NAME)).isNotNull();
//
//        // Get the storach
//        restStorageMockMvc.perform(delete("/api/storages/{id}", TO_DELETE_NAME)
//            .accept(TestUtil.APPLICATION_JSON_UTF8))
//            .andExpect(status().isOk());
//
//        // Validate the database is empty
//        assertThat(retrievalServer.getStorage(TO_DELETE_NAME)).isNull();
//    }
//
//    @Test
//    @Transactional
//    public void deleteStorageNotExist() throws Exception {
//
//        try {
//            MvcResult result  = restStorageMockMvc.perform(delete("/api/storages/{id}", "unknown")
//                .accept(TestUtil.APPLICATION_JSON_UTF8)).andReturn();
//            assert false;
//        } catch(NestedServletException e) {
//            assertThat(e.getCause().getClass()).isEqualTo(ResourceNotFoundException.class);
//        }
//    }
}
