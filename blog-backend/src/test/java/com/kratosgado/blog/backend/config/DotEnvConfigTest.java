package com.kratosgado.blog.backend.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test to verify that .env file variables are being loaded correctly
 */
@SpringBootTest
public class DotEnvConfigTest {

    @Autowired
    private Environment environment;

    @Test
    public void testDatabaseUrlFromEnv() {
        String dbUrl = environment.getProperty("DB_URL");
        assertThat(dbUrl)
                .isNotNull()
                .contains("postgresql")
                .contains("blog_db");
        System.out.println("✓ DB_URL loaded: " + dbUrl);
    }

    @Test
    public void testDatabaseUserFromEnv() {
        String dbUser = environment.getProperty("DB_USER");
        assertThat(dbUser)
                .isNotNull()
                .isNotBlank();
        System.out.println("✓ DB_USER loaded: " + dbUser);
    }

    @Test
    public void testMongoUriFromEnv() {
        String mongoUri = environment.getProperty("MONGO_URI");
        assertThat(mongoUri)
                .isNotNull()
                .contains("mongodb");
        System.out.println("✓ MONGO_URI loaded: " + mongoUri);
    }

    @Test
    public void testMongoDbNameFromEnv() {
        String mongoDbName = environment.getProperty("MONGO_DB_NAME");
        assertThat(mongoDbName)
                .isNotNull()
                .isEqualTo("blog_nosql");
        System.out.println("✓ MONGO_DB_NAME loaded: " + mongoDbName);
    }

    @Test
    public void testJwtSecretFromEnv() {
        String jwtSecret = environment.getProperty("JWT_SECRET");
        assertThat(jwtSecret)
                .isNotNull()
                .isNotBlank();
        System.out.println("✓ JWT_SECRET loaded (length: " + jwtSecret.length() + " chars)");
    }

    @Test
    public void testJwtExpirationFromEnv() {
        String jwtExpiration = environment.getProperty("JWT_EXPIRATION");
        assertThat(jwtExpiration)
                .isNotNull()
                .isEqualTo("86400000");
        System.out.println("✓ JWT_EXPIRATION loaded: " + jwtExpiration);
    }

    @Test
    public void testCorsOriginsFromEnv() {
        String corsOrigins = environment.getProperty("CORS_ORIGINS");
        assertThat(corsOrigins)
                .isNotNull()
                .contains("localhost");
        System.out.println("✓ CORS_ORIGINS loaded: " + corsOrigins);
    }

    @Test
    public void testGoogleClientIdFromEnv() {
        String googleClientId = environment.getProperty("GOOGLE_CLIENT_ID");
        assertThat(googleClientId)
                .isNotNull()
                .isNotBlank();
        System.out.println("✓ GOOGLE_CLIENT_ID loaded: " + googleClientId);
    }

    @Test
    public void testAllRequiredEnvVariablesPresent() {
        String[] requiredVars = {
                "DB_URL", "DB_USER", "DB_PASS",
                "MONGO_URI", "MONGO_DB_NAME",
                "JWT_SECRET", "JWT_EXPIRATION",
                "CORS_ORIGINS",
                "GOOGLE_CLIENT_ID", "GOOGLE_CLIENT_SECRET"
        };

        System.out.println("\n=== Environment Variables Check ===");
        for (String var : requiredVars) {
            String value = environment.getProperty(var);
            boolean present = value != null && !value.isBlank();
            String status = present ? "✓" : "✗";
            String displayValue = var.contains("SECRET") || var.contains("PASS")
                    ? "****" : value;
            System.out.println(status + " " + var + ": " + displayValue);
            assertThat(value)
                    .as("Environment variable %s should be set", var)
                    .isNotNull()
                    .isNotBlank();
        }
        System.out.println("=== All required variables present ===\n");
    }
}
