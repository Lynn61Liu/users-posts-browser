package com.example.userspostsbrowser.importflow;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.userspostsbrowser.importflow.dto.ImportedPostRecord;
import com.example.userspostsbrowser.importflow.dto.JsonPlaceholderPostDto;

class PostMapperTests {

	@Test
	@DisplayName("TC-3.4: Post mapping preserves the required fields")
	void postMappingPreservesTheRequiredFields() {
		PostMapper mapper = new PostMapper();
		JsonPlaceholderPostDto dto = new JsonPlaceholderPostDto(
			1L,
			1L,
			"sunt aut facere repellat provident occaecati excepturi optio reprehenderit",
			"quia et suscipit"
		);

		ImportedPostRecord record = mapper.toImportedPostRecord(dto);

		assertEquals(1L, record.externalId());
		assertEquals(1L, record.userExternalId());
		assertEquals("sunt aut facere repellat provident occaecati excepturi optio reprehenderit", record.title());
		assertEquals("quia et suscipit", record.body());
	}
}
