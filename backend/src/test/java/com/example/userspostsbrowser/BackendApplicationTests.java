package com.example.userspostsbrowser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BackendApplicationTests {

	@Test
	@DisplayName("TC-1.1: Backend project starts successfully with DynamoDB configuration")
	void backendProjectStartsSuccessfully() {
		// If the Spring context cannot boot, this test class fails before this method runs.
	}
}
