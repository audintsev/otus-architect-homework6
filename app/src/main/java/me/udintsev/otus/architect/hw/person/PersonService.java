package me.udintsev.otus.architect.hw.person;

import io.r2dbc.spi.Row;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Service
public class PersonService {
    private final DatabaseClient databaseClient;

    private static final String TABLE_NAME = "person";
    private static final String COL_ID = "id";
    private static final String COL_EMAIL = "email";
    private static final String COL_FIRST_NAME = "first_name";
    private static final String COL_LAST_NAME = "last_name";

    private static final Function<Row, Person> MAPPER = row -> new Person(
            row.get(COL_ID, Long.class),
            row.get(COL_EMAIL, String.class),
            row.get(COL_FIRST_NAME, String.class),
            row.get(COL_LAST_NAME, String.class)
    );

    private static final String SELECT_BASE = "SELECT %s, %s, %s, %s from %s".formatted(
            COL_ID, COL_EMAIL, COL_FIRST_NAME, COL_LAST_NAME, TABLE_NAME);

    public PersonService(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    public Mono<Person> get(long id) {
        return databaseClient.sql("%s where %s=:id".formatted(SELECT_BASE, COL_ID))
                .bind("id", id)
                .map(MAPPER)
                .one();
    }

    public Mono<Person> getByEmail(String email) {
        return databaseClient.sql("%s where %s=:email".formatted(SELECT_BASE, COL_EMAIL))
                .bind("email", email)
                .map(MAPPER)
                .one();
    }

    public Flux<Person> list() {
        return databaseClient.sql(SELECT_BASE)
                .map(MAPPER)
                .all();
    }

    public Mono<Person> create(String email, String first, String last) {
        return databaseClient.sql("INSERT INTO %s (%s, %s, %s) VALUES (:email, :first, :last)".formatted(
                TABLE_NAME, COL_EMAIL, COL_FIRST_NAME, COL_LAST_NAME))
                .filter(statement -> statement.returnGeneratedValues("id"))
                .bind("email", email)
                .bind("first", first)
                .bind("last", last)
                .map(row -> new Person(row.get("id", Long.class), email, first, last))
                .first();
    }

    public Mono<Person> update(long id, String email, String first, String last) {
        return databaseClient.sql("UPDATE %s SET %s=:email, %s=:first, %s=:last WHERE %s=:id".formatted(
                TABLE_NAME, COL_EMAIL, COL_FIRST_NAME, COL_LAST_NAME, COL_ID))
                .bind("id", id)
                .bind("email", email)
                .bind("first", first)
                .bind("last", last)
                .fetch().rowsUpdated()
                .flatMap(rowsUpdated -> rowsUpdated > 0
                        ? Mono.just(new Person(id, email, first, last))
                        : Mono.empty()
                );
    }

    public Mono<Void> delete(long id) {
        return databaseClient.sql("DELETE FROM %s WHERE %s=:id".formatted(TABLE_NAME, COL_ID))
                .bind("id", id)
                .then();
    }
}
