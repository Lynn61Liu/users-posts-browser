package com.example.userspostsbrowser.query;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class QueryApiTests {

	private static final LocalDateTime FIXED_TIME = LocalDateTime.of(2026, 6, 8, 12, 0);

	@Container
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

	@DynamicPropertySource
	static void registerDataSourceProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
		registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
	}

	private final HttpClient httpClient = HttpClient.newHttpClient();
	private final ObjectMapper objectMapper = new ObjectMapper();

	@LocalServerPort
	int port;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@BeforeEach
	void resetDatabase() {
		jdbcTemplate.execute("truncate table posts, users, raw_source restart identity cascade");
		seedData();
	}

	@Test
	@DisplayName("TC-4.1: User list endpoint returns imported users")
	void userListEndpointReturnsImportedUsers() throws Exception {
		HttpResponse<String> response = get("/api/users");

		assertEquals(200, response.statusCode());

		JsonNode body = objectMapper.readTree(response.body());
		assertEquals(2, body.size());
		assertAll(
			() -> assertEquals(1L, body.get(0).get("id").asLong()),
			() -> assertEquals("Leanne Graham", body.get(0).get("name").asText()),
			() -> assertEquals("Bret", body.get(0).get("username").asText()),
			() -> assertEquals("leanne@example.com", body.get(0).get("email").asText()),
			() -> assertEquals("Romaguera-Crona", body.get(0).get("companyName").asText()),
			() -> assertEquals(2L, body.get(1).get("id").asLong()),
			() -> assertEquals("Ervin Howell", body.get(1).get("name").asText()),
			() -> assertEquals("Antonette", body.get(1).get("username").asText()),
			() -> assertEquals("ervin@example.com", body.get(1).get("email").asText()),
			() -> assertEquals("Deckow-Crist", body.get(1).get("companyName").asText())
		);
	}

	@Test
	@DisplayName("TC-4.2: User detail endpoint returns one selected user")
	void userDetailEndpointReturnsOneSelectedUser() throws Exception {
		HttpResponse<String> response = get("/api/users/1");

		assertEquals(200, response.statusCode());

		JsonNode body = objectMapper.readTree(response.body());
		assertAll(
			() -> assertEquals(1L, body.get("id").asLong()),
			() -> assertEquals(1L, body.get("externalId").asLong()),
			() -> assertEquals("Leanne Graham", body.get("name").asText()),
			() -> assertEquals("Bret", body.get("username").asText()),
			() -> assertEquals("leanne@example.com", body.get("email").asText()),
			() -> assertEquals("1-770-736-8031 x56442", body.get("phone").asText()),
			() -> assertEquals("hildegard.org", body.get("website").asText()),
			() -> assertEquals("Kulas Light", body.get("address").get("street").asText()),
			() -> assertEquals("Apt. 556", body.get("address").get("suite").asText()),
			() -> assertEquals("Gwenborough", body.get("address").get("city").asText()),
			() -> assertEquals("92998-3874", body.get("address").get("zipcode").asText()),
			() -> assertEquals("-37.3159", body.get("address").get("geo").get("lat").asText()),
			() -> assertEquals("81.1496", body.get("address").get("geo").get("lng").asText()),
			() -> assertEquals("Romaguera-Crona", body.get("company").get("name").asText()),
			() -> assertEquals("Multi-layered client-server neural-net", body.get("company").get("catchPhrase").asText()),
			() -> assertEquals("harness real-time e-markets", body.get("company").get("bs").asText())
		);
	}

	@Test
	@DisplayName("TC-4.2: Missing user returns a clear error")
	void missingUserReturnsAClearError() throws Exception {
		HttpResponse<String> response = get("/api/users/999");

		assertEquals(404, response.statusCode());

		JsonNode body = objectMapper.readTree(response.body());
		assertAll(
			() -> assertEquals(404, body.get("status").asInt()),
			() -> assertEquals("Not Found", body.get("error").asText()),
			() -> assertTrue(body.get("message").asText().contains("999")),
			() -> assertEquals("/api/users/999", body.get("path").asText())
		);
	}

	@Test
	@DisplayName("TC-4.3: User posts endpoint returns related posts")
	void userPostsEndpointReturnsRelatedPosts() throws Exception {
		HttpResponse<String> response = get("/api/users/1/posts");

		assertEquals(200, response.statusCode());

		JsonNode body = objectMapper.readTree(response.body());
		assertEquals(2, body.size());
		assertAll(
			() -> assertEquals(1L, body.get(0).get("id").asLong()),
			() -> assertEquals("sunt aut facere repellat provident occaecati excepturi optio reprehenderit", body.get(0).get("title").asText()),
			() -> assertEquals(2L, body.get(1).get("id").asLong()),
			() -> assertEquals("qui est esse", body.get(1).get("title").asText())
		);
	}

	@Test
	@DisplayName("TC-4.3: User posts endpoint returns an empty list when no posts exist")
	void userPostsEndpointReturnsEmptyListWhenNoPostsExist() throws Exception {
		HttpResponse<String> response = get("/api/users/2/posts");

		assertEquals(200, response.statusCode());
		assertEquals(0, objectMapper.readTree(response.body()).size());
	}

	@Test
	@DisplayName("TC-4.4: API errors are returned in a consistent shape")
	void apiErrorsAreReturnedInAConsistentShape() throws Exception {
		HttpResponse<String> response = get("/api/users/999/posts");

		assertEquals(404, response.statusCode());

		JsonNode body = objectMapper.readTree(response.body());
		assertAll(
			() -> assertEquals(404, body.get("status").asInt()),
			() -> assertEquals("Not Found", body.get("error").asText()),
			() -> assertTrue(body.get("message").asText().contains("999")),
			() -> assertTrue(body.get("path").asText().endsWith("/api/users/999/posts"))
		);
	}

	private HttpResponse<String> get(String path) throws Exception {
		HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
			.GET()
			.build();
		return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
	}

	private void seedData() {
		long userOneRawSourceId = insertRawSource("user", 1L, "{\"id\":1}", "user-hash-1");
		long userTwoRawSourceId = insertRawSource("user", 2L, "{\"id\":2}", "user-hash-2");

		long userOneId = insertUser(
			userOneRawSourceId,
			1L,
			"Leanne Graham",
			"Bret",
			"leanne@example.com",
			"1-770-736-8031 x56442",
			"hildegard.org",
			"Kulas Light",
			"Apt. 556",
			"Gwenborough",
			"92998-3874",
			"-37.3159",
			"81.1496",
			"Romaguera-Crona",
			"Multi-layered client-server neural-net",
			"harness real-time e-markets"
		);
		insertUser(
			userTwoRawSourceId,
			2L,
			"Ervin Howell",
			"Antonette",
			"ervin@example.com",
			"010-692-6593 x09125",
			"anastasia.net",
			"Victor Plains",
			"Suite 879",
			"Wisokyburgh",
			"90566-7771",
			"-43.9509",
			"-34.4618",
			"Deckow-Crist",
			"Proactive didactic contingency",
			"synergize scalable supply-chains"
		);

		insertPost(
			insertRawSource("post", 1L, "{\"id\":1}", "post-hash-1"),
			userOneId,
			1L,
			"sunt aut facere repellat provident occaecati excepturi optio reprehenderit",
			"quia et suscipit\\nsuscipit recusandae consequuntur expedita et cum"
		);
		insertPost(
			insertRawSource("post", 2L, "{\"id\":2}", "post-hash-2"),
			userOneId,
			2L,
			"qui est esse",
			"est rerum tempore vitae\\nsequi sint nihil reprehenderit dolor beatae ea dolores neque"
		);
	}

	private long insertRawSource(String sourceType, long externalId, String rawPayload, String payloadHash) {
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(connection -> {
			PreparedStatement statement = connection.prepareStatement("""
				insert into raw_source (
					source_type,
					external_id,
					raw_payload,
					payload_hash,
					synced_at,
					sync_result,
					sync_batch_id
				)
				values (?, ?, ?::jsonb, ?, ?, ?, ?)
				""", Statement.RETURN_GENERATED_KEYS);
			statement.setString(1, sourceType);
			statement.setLong(2, externalId);
			statement.setString(3, rawPayload);
			statement.setString(4, payloadHash);
			statement.setTimestamp(5, Timestamp.valueOf(FIXED_TIME));
			statement.setString(6, "success");
			statement.setString(7, "batch-1");
			return statement;
		}, keyHolder);

		Number key = (Number) keyHolder.getKeys().get("id");
		assertFalse(key == null);
		return key.longValue();
	}

	private long insertUser(
		long rawSourceId,
		long externalId,
		String name,
		String username,
		String email,
		String phone,
		String website,
		String addressStreet,
		String addressSuite,
		String addressCity,
		String addressZipcode,
		String addressGeoLat,
		String addressGeoLng,
		String companyName,
		String companyCatchPhrase,
		String companyBs
	) {
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(connection -> {
			PreparedStatement statement = connection.prepareStatement("""
				insert into users (
					external_id,
					raw_source_id,
					name,
					username,
					email,
					phone,
					website,
					address_street,
					address_suite,
					address_city,
					address_zipcode,
					address_geo_lat,
					address_geo_lng,
					company_name,
					company_catch_phrase,
					company_bs,
					created_at,
					updated_at
				)
				values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
				""", Statement.RETURN_GENERATED_KEYS);
			statement.setLong(1, externalId);
			statement.setLong(2, rawSourceId);
			statement.setString(3, name);
			statement.setString(4, username);
			statement.setString(5, email);
			statement.setString(6, phone);
			statement.setString(7, website);
			statement.setString(8, addressStreet);
			statement.setString(9, addressSuite);
			statement.setString(10, addressCity);
			statement.setString(11, addressZipcode);
			statement.setString(12, addressGeoLat);
			statement.setString(13, addressGeoLng);
			statement.setString(14, companyName);
			statement.setString(15, companyCatchPhrase);
			statement.setString(16, companyBs);
			statement.setTimestamp(17, Timestamp.valueOf(FIXED_TIME));
			statement.setTimestamp(18, Timestamp.valueOf(FIXED_TIME));
			return statement;
		}, keyHolder);

		Number key = (Number) keyHolder.getKeys().get("id");
		assertFalse(key == null);
		return key.longValue();
	}

	private long insertPost(long rawSourceId, long userId, long externalId, String title, String body) {
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(connection -> {
			PreparedStatement statement = connection.prepareStatement("""
				insert into posts (
					external_id,
					raw_source_id,
					user_id,
					title,
					body,
					created_at,
					updated_at
				)
				values (?, ?, ?, ?, ?, ?, ?)
				""", Statement.RETURN_GENERATED_KEYS);
			statement.setLong(1, externalId);
			statement.setLong(2, rawSourceId);
			statement.setLong(3, userId);
			statement.setString(4, title);
			statement.setString(5, body);
			statement.setTimestamp(6, Timestamp.valueOf(FIXED_TIME));
			statement.setTimestamp(7, Timestamp.valueOf(FIXED_TIME));
			return statement;
		}, keyHolder);

		Number key = (Number) keyHolder.getKeys().get("id");
		assertFalse(key == null);
		return key.longValue();
	}
}
