package com.example.userspostsbrowser.importflow;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SyncFailureMessageTests {

	@Test
	@DisplayName("Database connection failures are translated into a user-friendly message")
	void databaseConnectionFailuresAreTranslated() {
		String message = SyncService.resolveErrorMessage(
			new RuntimeException("Could not open JPA EntityManager for transaction")
		);

		assertEquals(
			"Could not reach PostgreSQL. Make sure the database is running, then try again.",
			message
		);
	}

	@Test
	@DisplayName("Generic failures keep their original meaningful message")
	void genericFailuresKeepTheirMeaningfulMessage() {
		String message = SyncService.resolveErrorMessage(new RuntimeException("boom"));

		assertEquals("boom", message);
	}
}
