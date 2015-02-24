package org.cbir.retrieval.web.rest;

import org.cbir.retrieval.Application;
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
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.inject.Inject;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @Inject
    private RetrievalService retrievalService;

    private MockMvc restStorageMockMvc;

    @Before
    public void setup() {
        StorageResource storageResource = new StorageResource();
        ReflectionTestUtils.setField(storageResource, "retrievalService", retrievalService);
        this.restStorageMockMvc = MockMvcBuilders.standaloneSetup(storageResource).build();
    }

    @Test
    public void testGetStorage() throws Exception {
        restStorageMockMvc.perform(get("/api/storage/"+RetrievalService.DEFAULT_STORAGE).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.id").value(RetrievalService.DEFAULT_STORAGE));
    }

    @Test
    public void testGetStorageUnknown() throws Exception {
        restStorageMockMvc.perform(get("/api/storage/unknown")
            .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testListStorages() throws Exception {
        restStorageMockMvc.perform(get("/api/storage").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$[0].id").value(RetrievalService.DEFAULT_STORAGE));
    }




//    @Test
//    public void testGetExistingUser() throws Exception {
//        restStorageMockMvc.perform(get("/app/rest/users/admin")
//            .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType("application/json"))
//                .andExpect(jsonPath("$.lastName").value("Administrator"));
//    }
//
//    @Test
//    public void testGetUnknownUser() throws Exception {
//        restStorageMockMvc.perform(get("/app/rest/users/unknown")
//            .accept(MediaType.APPLICATION_JSON))
//                .andExpect(status().isNotFound());
//    }
}
