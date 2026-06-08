package com.example.userspostsbrowser.importflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class SyncControllerTests {

	private static final ExternalApiStubServer externalApi = ExternalApiStubServer.start();

	private static final String USERS_JSON = """
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

	private static final String POSTS_JSON = """
		[
		  {
		    "userId": 1,
		    "id": 1,
		    "title": "sunt aut facere repellat provident occaecati excepturi optio reprehenderit",
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

	private final HttpClient httpClient = HttpClient.newHttpClient();

	@LocalServerPort
	int port;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@AfterAll
	static void tearDownServer() {
		externalApi.close();
	}

	@Test
	@DisplayName("POST /api/sync triggers the import action")
	void postSyncTriggersTheImportAction() throws Exception {
		externalApi.setUsersResponse(200, USERS_JSON);
		externalApi.setPostsResponse(200, POSTS_JSON);

		HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/sync"))
			.header("Content-Type", "application/json")
			.POST(HttpRequest.BodyPublishers.noBody())
			.build();

		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

		assertEquals(200, response.statusCode());
		assertTrue(response.body().contains("\"status\":\"success\""));
		assertEquals(1L, jdbcTemplate.queryForObject("select count(*) from users", Long.class));
		assertEquals(1L, jdbcTemplate.queryForObject("select count(*) from posts", Long.class));
	}
}
