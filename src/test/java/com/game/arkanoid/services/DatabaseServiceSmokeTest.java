package com.game.arkanoid.services;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Smoke test that the service can be constructed and shut down without DB access.
 */
public class DatabaseServiceSmokeTest {

    @Test
    void constructAndShutdown() {
        DatabaseService svc = new DatabaseService();
        assertNotNull(svc);
        svc.shutdown();
    }
}

