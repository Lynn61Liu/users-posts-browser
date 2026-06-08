package com.example.userspostsbrowser.importflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PayloadHashServiceTests {

	@Test
	@DisplayName("TC-3.5: unchanged source data produces the same hash")
	void unchangedSourceDataProducesTheSameHash() {
		PayloadHashService hashService = new PayloadHashService();

		String firstHash = hashService.hashJson("""
			{"id":1,"name":"Leanne Graham"}
			""");
		String secondHash = hashService.hashJson("""
			{"id":1,"name":"Leanne Graham"}
			""");

		assertEquals(firstHash, secondHash);
	}

	@Test
	@DisplayName("TC-3.6: changed source data produces a different hash")
	void changedSourceDataProducesADifferentHash() {
		PayloadHashService hashService = new PayloadHashService();

		String firstHash = hashService.hashJson("""
			{"id":1,"name":"Leanne Graham"}
			""");
		String changedHash = hashService.hashJson("""
			{"id":1,"name":"Ervin Howell"}
			""");

		assertNotEquals(firstHash, changedHash);
	}
}
