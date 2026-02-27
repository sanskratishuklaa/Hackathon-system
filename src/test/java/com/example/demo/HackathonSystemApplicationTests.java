package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Smoke test — verifies the Spring application context loads correctly.
 * Uses the 'test' profile so H2 in-memory DB is used (no MySQL needed).
 */
@SpringBootTest
@ActiveProfiles("test")
class HackathonSystemApplicationTests {

	@Test
	void contextLoads() {
		// If this test passes, the entire Spring context (beans, security,
		// JPA, etc.) initialised without errors.
	}
}
