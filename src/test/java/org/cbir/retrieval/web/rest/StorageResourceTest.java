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
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.NestedServletException;
import retrieval.server.RetrievalServer;
import retrieval.storage.Storage;

import javax.inject.Inject;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the UserResource REST controller.
 *
 * @see org.cbir.retrieval.web.rest.UserResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class StorageResourceTest {

    private static final String DEFAULT_NAME = "SAMPLE_NAME";
    private static final String TO_DELETE_NAME = "TO_DELETE_NAME";
    private static final int NUMBER_OF_STORAGE_AT_BOOTSTRAT = 1;

    @Inject
    private RetrievalService retrievalService;

    private RetrievalServer retrievalServer;

    private MockMvc restStorageMockMvc;


    @Before
    public void setup() {
        StorageResource storageResource = new StorageResource();
        ReflectionTestUtils.setField(storageResource, "retrievalService", retrievalService);
        this.restStorageMockMvc = MockMvcBuilders.standaloneSetup(storageResource).build();
        this.retrievalServer = retrievalService.getRetrievalServer();
    }

    @Test
    @Transactional
    public void createStorage() throws Exception {
        // Validate the database is empty (only default storage)
        assertThat(retrievalServer.getStorageList()).hasSize(NUMBER_OF_STORAGE_AT_BOOTSTRAT);
        String name = "NEW_NAME";

        // Create the Storage
        MvcResult result = restStorageMockMvc.perform(post("/api/storages")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content("{\"id\":\"" + name + "\"}")).andReturn();

//        System.out.println(result.getResolvedException().getMessage());
//        System.out.println(result.getResolvedException().toString());

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        //System.out.println(result.andReturn().getResponse().getContentAsString());

        // Validate the Storach in the database
        List<Storage> storages = retrievalServer.getStorageList();
        assertThat(storages).hasSize(NUMBER_OF_STORAGE_AT_BOOTSTRAT + 1);
        Storage storage = retrievalServer.getStorage(name);
        assertThat(storage.getStorageName()).isEqualTo(name);
        assertThat(storage.getNumberOfItem()).isEqualTo(0);
    }

    @Test
    @Transactional
    public void createStorageAlreadyExist() throws Exception {
        // Validate the database is empty (only default storage)
        String name = retrievalServer.getStorageList().get(0).getStorageName();
        int status = -1;
        // Create the Storage
        System.out.println("***********************");
        try {
            MvcResult result = restStorageMockMvc.perform(post("/api/storages")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(String.format("{\"id\":\"%s\"}", name))).andReturn();
            assert false;
        } catch (NestedServletException e) {
            assertThat(e.getCause().getClass()).isEqualTo(ResourceAlreadyExistException.class);
        }
    }

    @Test
    public void testGetAllStorages() throws Exception {
        restStorageMockMvc.perform(get("/api/storages").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$[0].id").value(RetrievalService.DEFAULT_TEST_STORAGE));
    }


    @Test
    public void testGetStorage() throws Exception {
        restStorageMockMvc.perform(get("/api/storages/{id}", RetrievalService.DEFAULT_TEST_STORAGE).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.id").value(RetrievalService.DEFAULT_TEST_STORAGE));
    }

    @Test
    public void testGetStorageUnknown() throws Exception {
        try {
            restStorageMockMvc.perform(get("/api/storages/unknown")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
            assert false;
        } catch (NestedServletException e) {
            assertThat(e.getCause().getClass()).isEqualTo(ResourceNotFoundException.class);
        }
    }

    @Test
    @Transactional
    public void deleteStorage() throws Exception {
        retrievalServer.createStorage(TO_DELETE_NAME);

        assertThat(retrievalServer.getStorage(TO_DELETE_NAME)).isNotNull();

        // Get the storach
        restStorageMockMvc.perform(delete("/api/storages/{id}", TO_DELETE_NAME)
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        assertThat(retrievalServer.getStorage(TO_DELETE_NAME)).isNull();
    }

    @Test
    @Transactional
    public void deleteStorageNotExist() throws Exception {

        try {
            MvcResult result = restStorageMockMvc.perform(delete("/api/storages/{id}", "unknown")
                .accept(TestUtil.APPLICATION_JSON_UTF8)).andReturn();
            assert false;
        } catch (NestedServletException e) {
            assertThat(e.getCause().getClass()).isEqualTo(ResourceNotFoundException.class);
        }
    }
}
