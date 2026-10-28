package de.ghostnet.persistence;

import java.util.Map;

public record DatabaseConfig(String url, String user, String password) {

    public DatabaseConfig {
        if (url == null || !url.startsWith("jdbc:postgresql:")) {
            throw new IllegalArgumentException("Eine PostgreSQL-JDBC-Adresse ist erforderlich.");
        }
        if (user == null || user.isBlank() || password == null || password.isBlank()) {
            throw new IllegalArgumentException("Datenbankbenutzer und Passwort sind erforderlich.");
        }
    }

    public static DatabaseConfig fromEnvironment() {
        return new DatabaseConfig(
                setting("ghostnet.db.url", "GHOSTNET_DB_URL"),
                setting("ghostnet.db.user", "GHOSTNET_DB_USER"),
                setting("ghostnet.db.password", "GHOSTNET_DB_PASSWORD"));
    }

    public Map<String, Object> properties() {
        return Map.of(
                "jakarta.persistence.jdbc.driver", "org.postgresql.Driver",
                "jakarta.persistence.jdbc.url", url,
                "jakarta.persistence.jdbc.user", user,
                "jakarta.persistence.jdbc.password", password);
    }

    private static String setting(String property, String environment) {
        String value = System.getProperty(property);
        return value == null ? System.getenv(environment) : value;
    }

    @Override
    public String toString() {
        return "DatabaseConfig[Verbindungsdaten ausgeblendet]";
    }
}
