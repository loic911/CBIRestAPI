package org.cbir.retrieval.web.rest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.List;

import org.cbir.retrieval.Application;
import org.cbir.retrieval.domain.Storach;
import org.cbir.retrieval.repository.StorachRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the StorachResource REST controller.
 *
 * @see StorachResource
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class StorachResourceTest {

    private static final String DEFAULT_NAME = "SAMPLE_TEXT";
    private static final String UPDATED_NAME = "UPDATED_TEXT";
    
    private static final Long DEFAULT_SIZE = 0L;
    private static final Long UPDATED_SIZE = 1L;
    

    @Inject
    private StorachRepository storachRepository;

    private MockMvc restStorachMockMvc;

    private Storach storach;

    @PostConstruct
    public void setup() {
        MockitoAnnotations.initMocks(this);
        StorachResource storachResource = new StorachResource();
        ReflectionTestUtils.setField(storachResource, "storachRepository", storachRepository);
        this.restStorachMockMvc = MockMvcBuilders.standaloneSetup(storachResource).build();
    }

    @Before
    public void initTest() {
        storach = new Storach();
        storach.setName(DEFAULT_NAME);
        storach.setSize(DEFAULT_SIZE);
    }

    @Test
    @Transactional
    public void createStorach() throws Exception {
        // Validate the database is empty
        assertThat(storachRepository.findAll()).hasSize(0);

        // Create the Storach
        restStorachMockMvc.perform(post("/app/rest/storachs")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(storach)))
                .andExpect(status().isOk());

        // Validate the Storach in the database
        List<Storach> storachs = storachRepository.findAll();
        assertThat(storachs).hasSize(1);
        Storach testStorach = storachs.iterator().next();
        assertThat(testStorach.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testStorach.getSize()).isEqualTo(DEFAULT_SIZE);
    }

    @Test
    @Transactional
    public void getAllStorachs() throws Exception {
        // Initialize the database
        storachRepository.saveAndFlush(storach);

        // Get all the storachs
        restStorachMockMvc.perform(get("/app/rest/storachs"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.[0].id").value(storach.getId().intValue()))
                .andExpect(jsonPath("$.[0].name").value(DEFAULT_NAME.toString()))
                .andExpect(jsonPath("$.[0].size").value(DEFAULT_SIZE.intValue()));
    }

    @Test
    @Transactional
    public void getStorach() throws Exception {
        // Initialize the database
        storachRepository.saveAndFlush(storach);

        // Get the storach
        restStorachMockMvc.perform(get("/app/rest/storachs/{id}", storach.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(storach.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME.toString()))
            .andExpect(jsonPath("$.size").value(DEFAULT_SIZE.intValue()));
    }

    @Test
    @Transactional
    public void getNonExistingStorach() throws Exception {
        // Get the storach
        restStorachMockMvc.perform(get("/app/rest/storachs/{id}", 1L))
                .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateStorach() throws Exception {
        // Initialize the database
        storachRepository.saveAndFlush(storach);

        // Update the storach
        storach.setName(UPDATED_NAME);
        storach.setSize(UPDATED_SIZE);
        restStorachMockMvc.perform(post("/app/rest/storachs")
                .contentType(TestUtil.APPLICATION_JSON_UTF8)
                .content(TestUtil.convertObjectToJsonBytes(storach)))
                .andExpect(status().isOk());

        // Validate the Storach in the database
        List<Storach> storachs = storachRepository.findAll();
        assertThat(storachs).hasSize(1);
        Storach testStorach = storachs.iterator().next();
        assertThat(testStorach.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testStorach.getSize()).isEqualTo(UPDATED_SIZE);
    }

    @Test
    @Transactional
    public void deleteStorach() throws Exception {
        // Initialize the database
        storachRepository.saveAndFlush(storach);

        // Get the storach
        restStorachMockMvc.perform(delete("/app/rest/storachs/{id}", storach.getId())
                .accept(TestUtil.APPLICATION_JSON_UTF8))
                .andExpect(status().isOk());

        // Validate the database is empty
        List<Storach> storachs = storachRepository.findAll();
        assertThat(storachs).hasSize(0);
    }
}
