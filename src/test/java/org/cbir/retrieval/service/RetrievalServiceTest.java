package org.cbir.retrieval.service;

import org.cbir.retrieval.Application;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test class for the UserResource REST controller.
 *
 * @see org.cbir.retrieval.service.UserService
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@Transactional
public class RetrievalServiceTest {


    @Inject
    private RetrievalService retrievalService;

    @Test
    public void testRetrievalServer() {
        assertThat(retrievalService.getRetrievalServer()).isNotNull();
    }

}
