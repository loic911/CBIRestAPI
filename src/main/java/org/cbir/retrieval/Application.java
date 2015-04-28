package org.cbir.retrieval;
/*
 * Copyright (c) 2009-2015. Authors: see NOTICE file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import org.cbir.retrieval.config.Constants;
import org.cbir.retrieval.domain.User;
import org.cbir.retrieval.service.RetrievalService;
import org.cbir.retrieval.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.MetricFilterAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.MetricRepositoryAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.core.env.SimpleCommandLinePropertySource;
import retrieval.server.RetrievalServer;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

@ComponentScan
@EnableAutoConfiguration(exclude = {MetricFilterAutoConfiguration.class, MetricRepositoryAutoConfiguration.class})
public class Application {

    private final Logger log = LoggerFactory.getLogger(Application.class);

    @Inject
    private Environment env;

    @Inject
    private RetrievalService retrievalService;

    @Inject
    private UserService userService;

    /**
     * Initializes retrieval.
     * <p/>
     * Spring profiles can be configured with a program arguments --spring.profiles.active=your-active-profile
     * <p/>
     */
    @PostConstruct
    public void initApplication() throws IOException {
        log.info("*** initApplication ***");
        log.info("HOME="+new File("./").getAbsolutePath());
        log.info(System.getProperty("spring.profiles.active"));
        log.info(Arrays.toString(env.getActiveProfiles()));

        changePasswordIfNeeded();

        if (env.getActiveProfiles().length == 0) {
            log.warn("No Spring profile configured, running with default configuration");
        } else {
            log.info("Running with Spring profile(s) : {}", Arrays.toString(env.getActiveProfiles()));
        }

        try {
            log.info("init retrieval server");
            RetrievalServer server = retrievalService.initRetrievalServer();

            if(env.getProperty("retrieval.dataset.load").equals("true")) {
                retrievalService.indexDataset(server,Paths.get(env.getProperty("retrieval.dataset.path")));

            }
        } catch(Exception e) {
            log.error(e.toString());
            throw new IOException(e.getMessage());
        }


    }

    private void changePasswordIfNeeded() throws IOException {
        String filename = "password.txt";
        File passwordFile = new File(filename);
        log.info("check if " + passwordFile.getAbsolutePath() + " exists");
        if(passwordFile.exists()) {
            log.info("password file found, change password");
            Stream<String> lines = Files.lines(Paths.get(filename));
            Optional<String> hasPassword = lines.findFirst();
            if(hasPassword.isPresent()){
                User admin = userService.getUser("admin");
                userService.changePassword(admin,hasPassword.get());

                User user = userService.getUser("user");
                userService.changePassword(user,hasPassword.get());
            }
            lines.close();
            if(!passwordFile.delete()) {
                throw new IOException("Password file cannot be deleted:"+passwordFile.getAbsolutePath());
            }
        }


    }

    /**
     * Main method, used to run the application.
     */
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(Application.class);
        app.setShowBanner(false);

        SimpleCommandLinePropertySource source = new SimpleCommandLinePropertySource(args);

        // Check if the selected profile has been set as argument.
        // if not the development profile will be added
        addDefaultProfile(app, source);
        addLiquibaseScanPackages();
        app.run(args);
    }

    /**
     * Set a default profile if it has not been set
     */
    private static void addDefaultProfile(SpringApplication app, SimpleCommandLinePropertySource source) {

        if (!source.containsProperty("spring.profiles.active")) {
            app.setAdditionalProfiles(Constants.SPRING_PROFILE_DEVELOPMENT);
        }
    }

    /**
     * Set the liquibases.scan.packages to avoid an exception from ServiceLocator.
     */
    private static void addLiquibaseScanPackages() {
        System.setProperty("liquibase.scan.packages", "liquibase.change" + "," + "liquibase.database" + "," +
                "liquibase.parser" + "," + "liquibase.precondition" + "," + "liquibase.datatype" + "," +
                "liquibase.serializer" + "," + "liquibase.sqlgenerator" + "," + "liquibase.executor" + "," +
                "liquibase.snapshot" + "," + "liquibase.logging" + "," + "liquibase.diff" + "," +
                "liquibase.structure" + "," + "liquibase.structurecompare" + "," + "liquibase.lockservice" + "," +
                "liquibase.ext" + "," + "liquibase.changelog");
    }
}
