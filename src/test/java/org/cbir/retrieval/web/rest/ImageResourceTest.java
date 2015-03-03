package org.cbir.retrieval.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.cbir.retrieval.Application;
import org.cbir.retrieval.service.RetrievalService;
import org.cbir.retrieval.service.exception.ResourceAlreadyExistException;
import org.cbir.retrieval.service.exception.ResourceNotFoundException;
import org.json.JSONObject;
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
        "testdata/images/crop8.jpg",
        "testdata/images/crop1.jpg",
        "testdata/images/crop1.jpg"
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

        String storage = DEFAULT_STORAGE;
        Long id = 5l;

        File file = new File(IMAGE_PATHS[(int)(id-1)]);
        MockMultipartFile firstFile = new MockMultipartFile("file", file.getName(), "image/png", Files.readAllBytes(Paths.get(IMAGE_PATHS[(int)(id-1)])));

        MockMultipartFile file1 = new MockMultipartFile(file.getName(), Files.readAllBytes(Paths.get(IMAGE_PATHS[(int)(id-1)])));
        MvcResult result = restStorageMockMvc.perform(
            fileUpload("/api/images")
                .file(file1)
                .param("id", id+"")
                .param("storage", storage)
                .param("keys", "date;test")
                .param("values", "2015;test")
                .param("async", "false")
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id+""))
            .andExpect(jsonPath("$.test").value("test"))
            .andReturn();

        assertThat(retrievalServer.getStorage(storage).getProperties(id)).isNotNull();
        assertThat(retrievalServer.getStorage(storage).getProperties(id)).containsEntry("id",id+"");
    }

    @Test
    public void testAddImageNoProperties() throws Exception {
        // Validate the database is empty (only default storage)
        assertThat(retrievalServer.getSize()).isEqualTo(NUMBER_OF_PICTURES_AT_BEGINNING);

        String storage = DEFAULT_STORAGE;
        Long id = 6l;

        File file = new File(IMAGE_PATHS[(int)(id-1)]);
        MockMultipartFile firstFile = new MockMultipartFile("file", file.getName(), "image/png", Files.readAllBytes(Paths.get(IMAGE_PATHS[(int)(id-1)])));

        MockMultipartFile file1 = new MockMultipartFile(file.getName(), Files.readAllBytes(Paths.get(IMAGE_PATHS[(int)(id-1)])));
        MvcResult result = restStorageMockMvc.perform(
            fileUpload("/api/images")
                .file(file1)
                .param("id", id + "")
                .param("storage", storage)
                .param("async", "false")
        )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id+""))
            .andReturn();

        assertThat(retrievalServer.getStorage(storage).getProperties(id)).isNotNull();
        assertThat(retrievalServer.getStorage(storage).getProperties(id)).containsEntry("id",id+"");
    }


    @Test
    public void testAddImagStorageNotExist() throws Exception {
        // Validate the database is empty (only default storage)
        assertThat(retrievalServer.getSize()).isEqualTo(NUMBER_OF_PICTURES_AT_BEGINNING);

        String storage = "STORAGE_testAddImagStorageNotExist";
        Long id = 7l;

        assertThat(retrievalServer.getStorage(storage)).isNull();

        File file = new File(IMAGE_PATHS[(int)(id-1)]);
        MockMultipartFile firstFile = new MockMultipartFile("file", file.getName(), "image/png", Files.readAllBytes(Paths.get(IMAGE_PATHS[(int)(id-1)])));

        MockMultipartFile file1 = new MockMultipartFile(file.getName(), Files.readAllBytes(Paths.get(IMAGE_PATHS[(int)(id-1)])));
        MvcResult result = restStorageMockMvc.perform(
            fileUpload("/api/images")
                .file(file1)
                .param("id", id + "")
                .param("storage", storage)
                .param("async", "false")
        )
            .andReturn();

        printIfError(result);
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        Map<String, Object> map = parseStringToMap(result);
        assertThat(map.get("id")).isEqualTo(id+"");
        assertThat(retrievalServer.getStorage(storage)).isNotNull();
        assertThat(retrievalServer.getStorage(storage).getProperties(id)).isNotNull();
        assertThat(retrievalServer.getStorage(storage).getProperties(id)).containsEntry("id",id+"");
    }

    @Test
    public void testAddImageWithNoStorage() throws Exception {
        // Validate the database is empty (only default storage)
        assertThat(retrievalServer.getSize()).isEqualTo(NUMBER_OF_PICTURES_AT_BEGINNING);

        Long id = 8l;

        File file = new File(IMAGE_PATHS[(int)(id-1)]);
        MockMultipartFile firstFile = new MockMultipartFile("file", file.getName(), "image/png", Files.readAllBytes(Paths.get(IMAGE_PATHS[(int)(id-1)])));

        MockMultipartFile file1 = new MockMultipartFile(file.getName(), Files.readAllBytes(Paths.get(IMAGE_PATHS[(int)(id-1)])));
        MvcResult result = restStorageMockMvc.perform(
            fileUpload("/api/images")
                .file(file1)
                .param("id", id + "")
                .param("async", "false")
        )
            .andReturn();

        printIfError(result);
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        Map<String, Object> map = parseStringToMap(result);
        String storage = (String)map.get("storage");


        assertThat(retrievalServer.getStorage(storage)).isNotNull();
        assertThat(retrievalServer.getStorage(storage).getProperties(id)).isNotNull();
        assertThat(retrievalServer.getStorage(storage).getProperties(id)).containsEntry("id",id+"");
    }



    @Test
    public void testAddImageNoID() throws Exception {
        // Validate the database is empty (only default storage)
        assertThat(retrievalServer.getSize()).isEqualTo(NUMBER_OF_PICTURES_AT_BEGINNING);

        String storage = DEFAULT_STORAGE;

        File file = new File(IMAGE_PATHS[(int)(8)]);
        MockMultipartFile firstFile = new MockMultipartFile("file", file.getName(), "image/png", Files.readAllBytes(Paths.get(IMAGE_PATHS[(int)(8)])));

        MockMultipartFile file1 = new MockMultipartFile(file.getName(), Files.readAllBytes(Paths.get(IMAGE_PATHS[(int)(8)])));
        MvcResult result = restStorageMockMvc.perform(
            fileUpload("/api/images")
                .file(file1)
                    //.param("id", id + "")
                .param("storage", storage)
        )
            .andReturn();

        printIfError(result);
        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        Map<String, Object> map = parseStringToMap(result);
        Long id = Long.parseLong((String)map.get("id"));

        assertThat(retrievalServer.getStorage(storage).getProperties(id)).isNotNull();
        assertThat(retrievalServer.getStorage(storage).getProperties(id)).containsEntry("id",id+"");
    }




    private void printIfError(MvcResult result) {
        if(result.getResolvedException()!=null) {
            System.out.println(result.getResolvedException().getMessage());
            System.out.println(result.getResolvedException().toString());
        }
    }


    private Map<String, Object> parseStringToMap(MvcResult result) throws java.io.IOException {
        String response = result.getResponse().getContentAsString();
        ObjectReader reader = new ObjectMapper().reader(Map.class);
        return reader.readValue(response);
    }

}
