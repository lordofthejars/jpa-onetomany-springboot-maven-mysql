package com.hellokoding.jpa;

import com.hellokoding.jpa.model.Book;
import com.hellokoding.jpa.model.BookCategory;
import com.hellokoding.jpa.repository.BookCategoryRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.transaction.Transactional;
import org.arquillian.cube.docker.impl.client.containerobject.dsl.AwaitBuilder;
import org.arquillian.cube.docker.junit.rule.ContainerDslRule;
import org.assertj.core.api.Assertions;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
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
@ContextConfiguration(initializers = PersistenceJpaApplicationTests.Initializer.class)
public class PersistenceJpaApplicationTests {

    public static final String DB = "books";
    public static final String USERNAME = "postgres";
    public static final String PASSWORD = "postgres";

    @Autowired
    private BookCategoryRepository bookCategoryRepository;

    private static final Logger logger = LoggerFactory.getLogger(HelloJpaApplication.class);

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
        // save a couple of categories
        BookCategory categoryA = new BookCategory("Category A");
        Set bookAs = new HashSet<Book>() {{
            add(new Book("Book A1", categoryA));
            add(new Book("Book A2", categoryA));
            add(new Book("Book A3", categoryA));
        }};
        categoryA.setBooks(bookAs);

        BookCategory categoryB = new BookCategory("Category B");
        Set bookBs = new HashSet<Book>() {{
            add(new Book("Book B1", categoryB));
            add(new Book("Book B2", categoryB));
            add(new Book("Book B3", categoryB));
        }};
        categoryB.setBooks(bookBs);

        bookCategoryRepository.save(new HashSet<BookCategory>() {{
            add(categoryA);
            add(categoryB);
        }});

        final List<BookCategory> allCategories = bookCategoryRepository.findAll();

        assertThat(allCategories)
            .extracting(BookCategory::getName)
            .containsExactlyInAnyOrder("Category A", "Category B");

    }

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            EnvironmentTestUtils.addEnvironment("testcontainers", configurableApplicationContext.getEnvironment(),
                "spring.datasource.url=" + String.format("jdbc:postgresql://%s:%d/%s", postgresql.getIpAddress(), postgresql.getBindPort(5432), DB)
            );
        }
    }
}
