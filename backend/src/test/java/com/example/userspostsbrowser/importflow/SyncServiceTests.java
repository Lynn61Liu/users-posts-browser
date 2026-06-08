package com.example.userspostsbrowser.importflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
class SyncServiceTests {

	private static final ExternalApiStubServer externalApi = ExternalApiStubServer.start();

	private static final String INITIAL_USERS_JSON = """
		[
		  {
		    "id": 1,
		    "name": "Leanne Graham",
		    "username": "Bret",
		    "email": "leanne@example.com",
		    "phone": "1-770-736-8031 x56442",
		    "website": "hildegard.org",
		    "address": {
		      "street": "Kulas Light",
		      "suite": "Apt. 556",
		      "city": "Gwenborough",
		      "zipcode": "92998-3874",
		      "geo": {
		        "lat": "-37.3159",
		        "lng": "81.1496"
		      }
		    },
		    "company": {
		      "name": "Romaguera-Crona",
		      "catchPhrase": "Multi-layered client-server neural-net",
		      "bs": "harness real-time e-markets"
		    }
		  }
		]
		""";

	private static final String CHANGED_USERS_JSON = """
		[
		  {
		    "id": 1,
		    "name": "Leanne Graham Updated",
		    "username": "Bret",
		    "email": "leanne@example.com",
		    "phone": "1-770-736-8031 x56442",
		    "website": "hildegard.org",
		    "address": {
		      "street": "Kulas Light",
		      "suite": "Apt. 556",
		      "city": "Gwenborough",
		      "zipcode": "92998-3874",
		      "geo": {
		        "lat": "-37.3159",
		        "lng": "81.1496"
		      }
		    },
		    "company": {
		      "name": "Romaguera-Crona",
		      "catchPhrase": "Multi-layered client-server neural-net",
		      "bs": "harness real-time e-markets"
		    }
		  }
		]
		""";

	private static final String INITIAL_POSTS_JSON = """
		[
		  {
		    "userId": 1,
		    "id": 1,
		    "title": "sunt aut facere repellat provident occaecati excepturi optio reprehenderit",
		    "body": "quia et suscipit\\nsuscipit recusandae consequuntur expedita et cum"
		  }
		]
		""";

	private static final String CHANGED_POSTS_JSON = """
		[
		  {
		    "userId": 1,
		    "id": 1,
		    "title": "updated title",
		    "body": "quia et suscipit\\nsuscipit recusandae consequuntur expedita et cum"
		  }
		]
		""";

	@Container
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

	@DynamicPropertySource
	static void registerDataSourceProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
		registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
		registry.add("import.users-url", () -> externalApi.usersUri().toString());
		registry.add("import.posts-url", () -> externalApi.postsUri().toString());
		registry.add("import.request-timeout-ms", () -> 2000L);
	}

	@Autowired
	SyncService syncService;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@BeforeEach
	void resetDatabase() {
		jdbcTemplate.execute("truncate table posts, users, raw_source restart identity cascade");
	}

	@AfterAll
	static void tearDownServer() {
		externalApi.close();
	}

	@Test
	@DisplayName("TC-3.7: First sync stores imported data")
	void firstSyncStoresImportedData() {
		externalApi.setUsersResponse(200, INITIAL_USERS_JSON);
		externalApi.setPostsResponse(200, INITIAL_POSTS_JSON);

		SyncResult result = syncService.sync();

		assertEquals("success", result.status());
		assertTrue(result.message().contains("1 user"));
		assertEquals(1L, jdbcTemplate.queryForObject("select count(*) from users", Long.class));
		assertEquals(1L, jdbcTemplate.queryForObject("select count(*) from posts", Long.class));
		assertEquals(2L, jdbcTemplate.queryForObject("select count(*) from raw_source", Long.class));
	}

	@Test
	@DisplayName("TC-3.8: Re-running sync skips unchanged records")
	void rerunningSyncSkipsUnchangedRecords() {
		externalApi.setUsersResponse(200, INITIAL_USERS_JSON);
		externalApi.setPostsResponse(200, INITIAL_POSTS_JSON);
		syncService.sync();

		SyncResult result = syncService.sync();

		assertEquals("no_change", result.status());
		assertEquals(1L, jdbcTemplate.queryForObject("select count(*) from users", Long.class));
		assertEquals(1L, jdbcTemplate.queryForObject("select count(*) from posts", Long.class));
		assertEquals(2L, jdbcTemplate.queryForObject("select count(*) from raw_source", Long.class));
		assertEquals(2L, jdbcTemplate.queryForObject("""
			select count(*)
			from raw_source
			where sync_result = 'no_change'
			""", Long.class));
	}

	@Test
	@DisplayName("TC-3.9: Re-running sync updates changed records")
	void rerunningSyncUpdatesChangedRecords() {
		externalApi.setUsersResponse(200, INITIAL_USERS_JSON);
		externalApi.setPostsResponse(200, INITIAL_POSTS_JSON);
		syncService.sync();

		externalApi.setUsersResponse(200, CHANGED_USERS_JSON);
		externalApi.setPostsResponse(200, CHANGED_POSTS_JSON);
		SyncResult result = syncService.sync();

		assertEquals("update", result.status());
		Map<String, Object> userRow = jdbcTemplate.queryForMap("select name from users where external_id = 1");
		Map<String, Object> postRow = jdbcTemplate.queryForMap("select title from posts where external_id = 1");

		assertEquals("Leanne Graham Updated", userRow.get("name"));
		assertEquals("updated title", postRow.get("title"));
		assertEquals(2L, jdbcTemplate.queryForObject("""
			select count(*)
			from raw_source
			where sync_result = 'update'
			""", Long.class));
	}

	@Test
	@DisplayName("TC-3.10: Sync failure returns a clear error")
	void syncFailureReturnsAClearError() {
		externalApi.setUsersResponse(500, "{\"error\":\"boom\"}");
		externalApi.setPostsResponse(200, INITIAL_POSTS_JSON);

		SyncResult result = syncService.sync();

		assertEquals("error", result.status());
		assertTrue(result.message().toLowerCase().contains("boom"));
		assertEquals(0L, jdbcTemplate.queryForObject("select count(*) from raw_source", Long.class));
	}
}
