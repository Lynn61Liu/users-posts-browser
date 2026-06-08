package com.example.userspostsbrowser.importflow;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.userspostsbrowser.importflow.dto.ImportedUserRecord;
import com.example.userspostsbrowser.importflow.dto.JsonPlaceholderUserDto;

class UserMapperTests {

	@Test
	@DisplayName("TC-3.3: User mapping preserves the required fields")
	void userMappingPreservesTheRequiredFields() {
		UserMapper mapper = new UserMapper();
		JsonPlaceholderUserDto dto = new JsonPlaceholderUserDto(
			1L,
			"Leanne Graham",
			"Bret",
			"leanne@example.com",
			"1-770-736-8031 x56442",
			"hildegard.org",
			new JsonPlaceholderUserDto.AddressDto(
				"Kulas Light",
				"Apt. 556",
				"Gwenborough",
				"92998-3874",
				new JsonPlaceholderUserDto.GeoDto("-37.3159", "81.1496")
			),
			new JsonPlaceholderUserDto.CompanyDto(
				"Romaguera-Crona",
				"Multi-layered client-server neural-net",
				"harness real-time e-markets"
			)
		);

		ImportedUserRecord record = mapper.toImportedUserRecord(dto);

		assertEquals(1L, record.externalId());
		assertEquals("Leanne Graham", record.name());
		assertEquals("Bret", record.username());
		assertEquals("leanne@example.com", record.email());
		assertEquals("1-770-736-8031 x56442", record.phone());
		assertEquals("hildegard.org", record.website());
		assertEquals("Kulas Light", record.addressStreet());
		assertEquals("Apt. 556", record.addressSuite());
		assertEquals("Gwenborough", record.addressCity());
		assertEquals("92998-3874", record.addressZipcode());
		assertEquals("-37.3159", record.addressGeoLat());
		assertEquals("81.1496", record.addressGeoLng());
		assertEquals("Romaguera-Crona", record.companyName());
		assertEquals("Multi-layered client-server neural-net", record.companyCatchPhrase());
		assertEquals("harness real-time e-markets", record.companyBs());
	}
}
