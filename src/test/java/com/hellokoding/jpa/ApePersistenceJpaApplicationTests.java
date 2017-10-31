package com.hellokoding.jpa;

import com.hellokoding.jpa.model.BookCategory;
import com.hellokoding.jpa.repository.BookCategoryRepository;
import java.util.List;
import javax.transaction.Transactional;
import org.arquillian.ape.junit.rule.ArquillianPersistenceRule;
import org.arquillian.ape.rdbms.core.RdbmsPopulator;
import org.arquillian.ape.rdbms.dbunit.DbUnit;
import org.arquillian.cube.docker.impl.client.containerobject.dsl.AwaitBuilder;
import org.arquillian.cube.docker.junit.rule.ContainerDslRule;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.postgresql.Driver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.EnvironmentTestUtils;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(initializers = ApePersistenceJpaApplicationTests.Initializer.class)
public class ApePersistenceJpaApplicationTests {

    public static final String DB = "books";
    public static final String USERNAME = "postgres";
    public static final String PASSWORD = "postgres";

    @Autowired
    private BookCategoryRepository bookCategoryRepository;

    private static final Logger logger = LoggerFactory.getLogger(HelloJpaApplication.class);

    @Rule
    public ArquillianPersistenceRule arquillianPersistenceRule = new ArquillianPersistenceRule();

    @DbUnit
    @ArquillianResource
    RdbmsPopulator dbUnitRdbmsPopulator;

    @ClassRule
    public static ContainerDslRule postgresql = new ContainerDslRule("postgres:9.6.2-alpine")
        .withPortBinding("15432->5432")
        .withEnvironment("POSTGRES_PASSWORD", PASSWORD,
            "POSTGRES_USER", USERNAME,
            "POSTGRES_DB", DB)
        .withAwaitStrategy(AwaitBuilder.logAwait("LOG:  autovacuum launcher started", 2));

    @Test
    @Transactional
    public void should_find_all_categories() {

        dbUnitRdbmsPopulator.forUri(ApePersistenceJpaApplicationTests.Initializer.jdbc())
            .withDriver(Driver.class)
            .withUsername(USERNAME)
            .withPassword(PASSWORD)
            .usingDataSet("books.yml")
            .execute();

        final List<BookCategory> allCategories = bookCategoryRepository.findAll();

        assertThat(allCategories)
            .extracting(BookCategory::getName)
            .containsExactlyInAnyOrder("Category A", "Category B");
    }

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            EnvironmentTestUtils.addEnvironment("testcontainers", configurableApplicationContext.getEnvironment(),
                "spring.datasource.url=" + jdbc()
            );
        }

        public static String jdbc() {
            return String.format("jdbc:postgresql://%s:%d/%s", postgresql.getIpAddress(), postgresql.getBindPort(5432), DB);
        }
    }
}
