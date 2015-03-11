package org.cbir.retrieval.web.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.cbir.retrieval.Application;
import org.cbir.retrieval.service.RetrievalService;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.NestedServletException;
import retrieval.server.RetrievalServer;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the UserResource REST controller.
 *
 * @see org.cbir.retrieval.web.rest.UserResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class SearchRetrievalTest {

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
    public void setup() throws Exception {
        ImageResource imageResource = new ImageResource();
        ReflectionTestUtils.setField(imageResource, "retrievalService", retrievalService);
        this.restStorageMockMvc = MockMvcBuilders.standaloneSetup(imageResource).build();

        retrievalService.reset();
        this.retrievalServer = retrievalService.getRetrievalServer();

        for (int i = 1; i < 5; i++) {

            Long id = (long) i;
            BufferedImage img = ImageIO.read(new File(IMAGE_PATHS[i - 1]));
            Map<String, String> properties = new TreeMap<>();
            properties.put("path", IMAGE_PATHS[i - 1]);
            properties.put("date", new Date().toString());

            retrievalServer
                .getStorage(DEFAULT_STORAGE)
                .indexPicture(img, id, properties);
        }

    }

    @Test
    public void testSearchImage() throws Exception {
        // Validate the database is empty (only default storage)
        assertThat(retrievalServer.getSize()).isEqualTo(NUMBER_OF_PICTURES_AT_BEGINNING);

        Long id = 5l;

        File file = new File(IMAGE_PATHS[(int) (id - 1)]);
        MockMultipartFile firstFile = new MockMultipartFile("file", file.getName(), "image/png", Files.readAllBytes(Paths.get(IMAGE_PATHS[1])));

        MvcResult result = restStorageMockMvc.perform(
            fileUpload("/api/search")
                .file(firstFile)
        )
            .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(200);

        List<Map> results = ImageResourceTest.parseStringToList(result);
        assertThat(results.isEmpty()).isFalse();
    }

    @Test
    public void testSearchImageMax() throws Exception {
        // Validate the database is empty (only default storage)
        assertThat(retrievalServer.getSize()).isEqualTo(NUMBER_OF_PICTURES_AT_BEGINNING);

        Long id = 5l;

        File file = new File(IMAGE_PATHS[(int) (id - 1)]);
        MockMultipartFile firstFile = new MockMultipartFile("file", file.getName(), "image/png", Files.readAllBytes(Paths.get(IMAGE_PATHS[1])));

        MvcResult result = restStorageMockMvc.perform(
            fileUpload("/api/search")
                .file(firstFile)
                .param("max", "1")
        )
            .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(200);

        List<Map> results = ImageResourceTest.parseStringToList(result);
        assertThat(results.size()).isEqualTo(1);
    }

    @Test
    public void testSearchImageMaxWithSotrage() throws Exception {
        // Validate the database is empty (only default storage)
        assertThat(retrievalServer.getSize()).isEqualTo(NUMBER_OF_PICTURES_AT_BEGINNING);

        retrievalServer.createStorage("testSearchImageMax");
        Long id = 5l;

        File file = new File(IMAGE_PATHS[(int) (id - 1)]);
        MockMultipartFile firstFile = new MockMultipartFile("file", file.getName(), "image/png", Files.readAllBytes(Paths.get(IMAGE_PATHS[1])));

        MvcResult result = restStorageMockMvc.perform(
            fileUpload("/api/search")
                .file(firstFile)
                .param("max", "1")
                .param("storages", DEFAULT_STORAGE + ";testSearchImageMax")
        )
            .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(200);

        List<Map> results = ImageResourceTest.parseStringToList(result);
        assertThat(results.size()).isEqualTo(1);
    }


}
