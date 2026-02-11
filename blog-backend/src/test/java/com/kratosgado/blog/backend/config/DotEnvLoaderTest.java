package com.kratosgado.blog.backend.config;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Simple test to verify .env file can be loaded without full Spring context
 */
public class DotEnvLoaderTest {

    @Test
    public void testDotEnvFileCanBeLoaded() {
        // Try to load from parent directory (when running from blog-backend module)
        Dotenv dotenv = null;
        String loadedFrom = null;

        try {
            dotenv = Dotenv.configure()
                    .directory("./")
                    .load();
            loadedFrom = "./";
        } catch (DotenvException e) {
            try {
                dotenv = Dotenv.configure()
                        .directory("../")
                        .load();
                loadedFrom = "../";
            } catch (DotenvException ex) {
                throw new AssertionError(".env file not found in ./ or ../", ex);
            }
        }

        assertThat(dotenv).isNotNull();
        System.out.println("\n✓ Successfully loaded .env file from: " + loadedFrom);
        System.out.println("✓ Number of variables loaded: " + dotenv.entries().size());

        // Verify key environment variables are present
        assertThat(dotenv.get("DB_URL"))
                .isNotNull()
                .contains("postgresql");
        System.out.println("✓ DB_URL: " + dotenv.get("DB_URL"));

        assertThat(dotenv.get("DB_USER"))
                .isNotNull();
        System.out.println("✓ DB_USER: " + dotenv.get("DB_USER"));

        assertThat(dotenv.get("MONGO_URI"))
                .isNotNull()
                .contains("mongodb");
        System.out.println("✓ MONGO_URI: " + dotenv.get("MONGO_URI"));

        assertThat(dotenv.get("MONGO_DB_NAME"))
                .isNotNull();
        System.out.println("✓ MONGO_DB_NAME: " + dotenv.get("MONGO_DB_NAME"));

        // JWT variables might not be in .env (use defaults from application.yml)
        String jwtSecret = dotenv.get("JWT_SECRET");
        if (jwtSecret != null) {
            System.out.println("✓ JWT_SECRET: [REDACTED] (length: " + jwtSecret.length() + " chars)");
        } else {
            System.out.println("⚠ JWT_SECRET: not in .env (will use default from application.yml)");
        }

        String jwtExpiration = dotenv.get("JWT_EXPIRATION");
        if (jwtExpiration != null) {
            System.out.println("✓ JWT_EXPIRATION: " + jwtExpiration);
        } else {
            System.out.println("⚠ JWT_EXPIRATION: not in .env (will use default from application.yml)");
        }

        String corsOrigins = dotenv.get("CORS_ORIGINS");
        if (corsOrigins != null) {
            System.out.println("✓ CORS_ORIGINS: " + corsOrigins);
        } else {
            System.out.println("⚠ CORS_ORIGINS: not in .env (will use default from application.yml)");
        }

        assertThat(dotenv.get("GOOGLE_CLIENT_ID"))
                .isNotNull();
        System.out.println("✓ GOOGLE_CLIENT_ID: " + dotenv.get("GOOGLE_CLIENT_ID"));

        assertThat(dotenv.get("GOOGLE_CLIENT_SECRET"))
                .isNotNull();
        System.out.println("✓ GOOGLE_CLIENT_SECRET: [REDACTED]");

        System.out.println("\n=== All required .env variables verified ===\n");
    }
}
