package de.ghostnet.persistence;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class DatabaseConfigTest {

    @Test
    void rejectsMissingConfiguration() {
        assertThrows(IllegalArgumentException.class, () -> new DatabaseConfig(null, null, null));
        assertThrows(IllegalArgumentException.class,
                () -> new DatabaseConfig("jdbc:postgresql://localhost/test", " ", " "));
    }

    @Test
    void doesNotExposeConnectionDetailsInToString() {
        String generated = UUID.randomUUID().toString();
        DatabaseConfig config = new DatabaseConfig("jdbc:postgresql://localhost/test", generated, generated);
        assertFalse(config.toString().contains(generated));
        assertFalse(config.toString().contains("localhost"));
    }
}
