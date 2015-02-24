package org.cbir.retrieval.web.rest;

import org.cbir.retrieval.Application;
import org.cbir.retrieval.domain.Storach;
import org.cbir.retrieval.service.RetrievalService;
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
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
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

        // Create the Storach
        ResultActions result = restStorageMockMvc.perform(post("/api/storages/" + DEFAULT_NAME)
            //.accept(MediaType.APPLICATION_JSON)).andReturn();
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(new Storach())))
            .andExpect(status().isOk());
        System.out.println("*****************");
        System.out.println(result.andReturn().getResponse().getContentAsString());
//        System.out.println(result.getResponse().getContentAsString());
//        System.out.println(new String(result.getResponse().getContentAsByteArray()));
//        assertThat(result.getResponse().getStatus()).isEqualTo(200);

        // Validate the Storach in the database
        List<Storage> storages = retrievalServer.getStorageList();
        assertThat(storages).hasSize(NUMBER_OF_STORAGE_AT_BOOTSTRAT +1);
        Storage storage = retrievalServer.getStorage(DEFAULT_NAME);
        assertThat(storage.getStorageName()).isEqualTo(DEFAULT_NAME);
        assertThat(storage.getNumberOfItem()).isEqualTo(0);
    }

    @Test
    public void testGetAllStorages() throws Exception {
        restStorageMockMvc.perform(get("/api/storages").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$[0].id").value(RetrievalService.DEFAULT_STORAGE));
    }


    @Test
    public void testGetStorage() throws Exception {
        restStorageMockMvc.perform(get("/api/storages/{id}",RetrievalService.DEFAULT_STORAGE).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.id").value(RetrievalService.DEFAULT_STORAGE));
    }

    @Test
    public void testGetStorageUnknown() throws Exception {
        restStorageMockMvc.perform(get("/api/storages/unknown")
            .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
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

}
