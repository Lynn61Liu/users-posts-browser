package com.example.userspostsbrowser.devtools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDateTime;

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
class DevResetEndpointTests {

	private static final LocalDateTime FIXED_TIME = LocalDateTime.of(2026, 6, 8, 12, 0);

	@Container
	static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

	@DynamicPropertySource
	static void registerDataSourceProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
		registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
		registry.add("app.dev.reset-enabled", () -> true);
	}

	private final HttpClient httpClient = HttpClient.newHttpClient();

	@LocalServerPort
	int port;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Test
	@DisplayName("Dev reset endpoint clears raw_source, users, and posts")
	void devResetEndpointClearsTheDatabase() throws Exception {
		insertSampleData();

		HttpRequest request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/dev/reset"))
			.header("Content-Type", "application/json")
			.POST(HttpRequest.BodyPublishers.noBody())
			.build();

		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

		assertEquals(200, response.statusCode());
		assertTrue(response.body().contains("reset"));
		assertEquals(0L, jdbcTemplate.queryForObject("select count(*) from raw_source", Long.class));
		assertEquals(0L, jdbcTemplate.queryForObject("select count(*) from users", Long.class));
		assertEquals(0L, jdbcTemplate.queryForObject("select count(*) from posts", Long.class));
	}

	private void insertSampleData() {
		jdbcTemplate.update(
			"""
			insert into raw_source (
				id,
				source_type,
				external_id,
				raw_payload,
				payload_hash,
				synced_at,
				sync_result,
				sync_batch_id
			)
			values (?, ?, ?, ?::jsonb, ?, ?, ?, ?)
			""",
			1L,
			"user",
			1L,
			"{\"id\":1}",
			"hash-1",
			Timestamp.valueOf(FIXED_TIME),
			"success",
			"batch-1"
		);
		jdbcTemplate.update(
			"""
			insert into users (
				id,
				external_id,
				raw_source_id,
				name,
				username,
				email,
				created_at,
				updated_at
			)
			values (?, ?, ?, ?, ?, ?, ?, ?)
			""",
			1L,
			1L,
			1L,
			"Leanne Graham",
			"Bret",
			"leanne@example.com",
			Timestamp.valueOf(FIXED_TIME),
			Timestamp.valueOf(FIXED_TIME)
		);
		jdbcTemplate.update(
			"""
			insert into posts (
				id,
				external_id,
				raw_source_id,
				user_id,
				title,
				body,
				created_at,
				updated_at
			)
			values (?, ?, ?, ?, ?, ?, ?, ?)
			""",
			1L,
			1L,
			1L,
			1L,
			"Post title",
			"Post body",
			Timestamp.valueOf(FIXED_TIME),
			Timestamp.valueOf(FIXED_TIME)
		);
	}
}
