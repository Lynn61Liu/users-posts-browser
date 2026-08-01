package com.example.userspostsbrowser.importflow;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SyncFailureMessageTests {

	@Test
	@DisplayName("Generic failures keep their original meaningful message")
	void genericFailuresKeepTheirMeaningfulMessage() {
		String message = SyncService.resolveErrorMessage(new RuntimeException("boom"));

		assertEquals("boom", message);
	}
}
