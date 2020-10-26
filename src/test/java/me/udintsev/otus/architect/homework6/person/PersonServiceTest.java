package me.udintsev.otus.architect.homework6.person;

import me.udintsev.otus.architect.homework6.DatabaseConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.test.StepVerifier;

import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = DatabaseConfig.class)
class PersonServiceTest {
    @Autowired
    PersonService personService;

    @Test
    void insertAndList() {
        Consumer<Person> person1Verifier = person -> {
            assertThat(person).isNotNull();
            assertThat(person.getId()).isNotNull();
            assertThat(person.getFirst()).isEqualTo("SomeFirst");
            assertThat(person.getLast()).isEqualTo("SomeLast");
        };
        personService.create("SomeFirst", "SomeLast").as(StepVerifier::create)
                .assertNext(person1Verifier)
                .verifyComplete();

        Consumer<Person> person2Verifier = person -> {
            assertThat(person).isNotNull();
            assertThat(person.getId()).isNotNull();
            assertThat(person.getFirst()).isEqualTo("AnotherFirst");
            assertThat(person.getLast()).isEqualTo("AnotherLast");
        };
        personService.create("AnotherFirst", "AnotherLast").as(StepVerifier::create)
                .assertNext(person2Verifier)
                .verifyComplete();

        personService.list().as(StepVerifier::create)
                .assertNext(person1Verifier)
                .assertNext(person2Verifier)
                .verifyComplete();
    }
}