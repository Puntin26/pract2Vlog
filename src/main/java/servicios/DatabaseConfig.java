package servicios;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public final class DatabaseConfig {
    private static final String EXTERNAL_CONFIG_PATH = "config/database.properties";

    private DatabaseConfig() {
    }

    public static Map<String, String> getJpaOverrides() {
        Properties externalProperties = loadExternalProperties();

        String jdbcUrl = resolve(externalProperties, "db.url", "COCKROACH_DB_URL", "DB_URL");
        if (jdbcUrl == null || jdbcUrl.isBlank()) {
            return Map.of();
        }

        Map<String, String> overrides = new HashMap<>();
        overrides.put("jakarta.persistence.jdbc.url", jdbcUrl);
        overrides.put("jakarta.persistence.jdbc.driver", resolve(externalProperties, "db.driver", "DB_DRIVER", "org.postgresql.Driver"));
        overrides.put("jakarta.persistence.jdbc.user", resolve(externalProperties, "db.user", "COCKROACH_DB_USER", "DB_USER", ""));
        overrides.put("jakarta.persistence.jdbc.password", resolve(externalProperties, "db.password", "COCKROACH_DB_PASSWORD", "DB_PASSWORD", ""));
        overrides.put("hibernate.dialect", resolve(externalProperties, "hibernate.dialect", "HIBERNATE_DIALECT", "org.hibernate.dialect.PostgreSQLDialect"));

        return overrides;
    }

    private static Properties loadExternalProperties() {
        Properties properties = new Properties();
        Path configPath = Path.of(EXTERNAL_CONFIG_PATH);

        if (!Files.exists(configPath)) {
            return properties;
        }

        try (InputStream inputStream = Files.newInputStream(configPath)) {
            properties.load(inputStream);
        } catch (IOException e) {
            System.err.println("No se pudo leer " + EXTERNAL_CONFIG_PATH + ": " + e.getMessage());
        }

        return properties;
    }

    private static String resolve(Properties properties, String propertyKey, String envKey, String fallbackEnvKey) {
        String value = properties.getProperty(propertyKey);
        if (value != null && !value.isBlank()) {
            return value;
        }

        value = System.getenv(envKey);
        if (value != null && !value.isBlank()) {
            return value;
        }

        value = System.getenv(fallbackEnvKey);
        if (value != null && !value.isBlank()) {
            return value;
        }

        return null;
    }

    private static String resolve(Properties properties, String propertyKey, String envKey, String fallbackEnvKey, String defaultValue) {
        String value = resolve(properties, propertyKey, envKey, fallbackEnvKey);
        return value != null ? value : defaultValue;
    }
}
