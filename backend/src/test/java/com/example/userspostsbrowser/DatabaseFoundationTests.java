package com.example.userspostsbrowser;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Map;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.postgresql.util.PGobject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
class DatabaseFoundationTests {

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

	@Autowired
	DataSource dataSource;

	@Autowired
	JdbcTemplate jdbcTemplate;

	@Test
	@DisplayName("TC-2.1: raw_source table stores one raw record per imported item")
	void rawSourceTableStoresOneRawRecordPerImportedItem() {
		long userRawSourceId = insertRawSource("user", 1L, "{\"id\":1,\"kind\":\"user\"}", "user-hash-1", "success");
		long postRawSourceId = insertRawSource("post", 1L, "{\"id\":1,\"kind\":\"post\"}", "post-hash-1", "success");

		Map<String, Object> userRow = jdbcTemplate.queryForMap("""
			select source_type, external_id, raw_payload::text as raw_payload, payload_hash
			from raw_source
			where id = ?
			""", userRawSourceId);
		Map<String, Object> postRow = jdbcTemplate.queryForMap("""
			select source_type, external_id, raw_payload::text as raw_payload, payload_hash
			from raw_source
			where id = ?
			""", postRawSourceId);

		assertAll(
			() -> assertEquals("user", userRow.get("source_type")),
			() -> assertEquals(1L, ((Number) userRow.get("external_id")).longValue()),
			() -> assertEquals("user-hash-1", userRow.get("payload_hash")),
			() -> assertNotNull(userRow.get("raw_payload")),
			() -> assertEquals("post", postRow.get("source_type")),
			() -> assertEquals(1L, ((Number) postRow.get("external_id")).longValue()),
			() -> assertEquals("post-hash-1", postRow.get("payload_hash")),
			() -> assertNotNull(postRow.get("raw_payload"))
		);
	}

	@Test
	@DisplayName("TC-2.2: users table stores expanded user fields")
	void usersTableStoresExpandedUserFields() {
		long rawSourceId = insertRawSource("user", 7L, "{\"id\":7}", "user-hash-7", "success");
		long userId = insertUser(rawSourceId, 7L);

		Map<String, Object> row = jdbcTemplate.queryForMap("""
			select external_id,
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
			from users
			where id = ?
			""", userId);

		assertAll(
			() -> assertEquals(7L, ((Number) row.get("external_id")).longValue()),
			() -> assertEquals(rawSourceId, ((Number) row.get("raw_source_id")).longValue()),
			() -> assertEquals("Leanne Graham", row.get("name")),
			() -> assertEquals("Bret", row.get("username")),
			() -> assertEquals("leanne@example.com", row.get("email")),
			() -> assertEquals("1-770-736-8031 x56442", row.get("phone")),
			() -> assertEquals("hildegard.org", row.get("website")),
			() -> assertEquals("Kulas Light", row.get("address_street")),
			() -> assertEquals("Apt. 556", row.get("address_suite")),
			() -> assertEquals("Gwenborough", row.get("address_city")),
			() -> assertEquals("92998-3874", row.get("address_zipcode")),
			() -> assertEquals("-37.3159", row.get("address_geo_lat")),
			() -> assertEquals("81.1496", row.get("address_geo_lng")),
			() -> assertEquals("Romaguera-Crona", row.get("company_name")),
			() -> assertEquals("Multi-layered client-server neural-net", row.get("company_catch_phrase")),
			() -> assertEquals("harness real-time e-markets", row.get("company_bs")),
			() -> assertEquals(Timestamp.valueOf(FIXED_TIME), row.get("created_at")),
			() -> assertEquals(Timestamp.valueOf(FIXED_TIME), row.get("updated_at"))
		);
	}

	@Test
	@DisplayName("TC-2.3: posts table stores post fields and user relation")
	void postsTableStoresPostFieldsAndUserRelation() {
		long rawSourceId = insertRawSource("post", 11L, "{\"id\":11}", "post-hash-11", "success");
		long userId = insertUser(insertRawSource("user", 11L, "{\"id\":11}", "user-hash-11", "success"), 11L);
		long postId = insertPost(rawSourceId, userId, 11L);

		Map<String, Object> row = jdbcTemplate.queryForMap("""
			select external_id,
			       raw_source_id,
			       user_id,
			       title,
			       body,
			       created_at,
			       updated_at
			from posts
			where id = ?
			""", postId);

		assertAll(
			() -> assertEquals(11L, ((Number) row.get("external_id")).longValue()),
			() -> assertEquals(rawSourceId, ((Number) row.get("raw_source_id")).longValue()),
			() -> assertEquals(userId, ((Number) row.get("user_id")).longValue()),
			() -> assertEquals("sunt aut facere repellat provident occaecati excepturi optio reprehenderit", row.get("title")),
			() -> assertEquals("quia et suscipit\nsuscipit recusandae consequuntur expedita et cum\nreprehenderit molestiae ut ut quas totam\nnostrum rerum est autem sunt rem eveniet architecto", row.get("body")),
			() -> assertEquals(Timestamp.valueOf(FIXED_TIME), row.get("created_at")),
			() -> assertEquals(Timestamp.valueOf(FIXED_TIME), row.get("updated_at"))
		);
	}

	@Test
	@DisplayName("TC-2.4: database constraints prevent duplicates")
	void databaseConstraintsPreventDuplicates() throws Exception {
		assertAll(
			() -> assertPrimaryKeyExists("raw_source"),
			() -> assertPrimaryKeyExists("users"),
			() -> assertPrimaryKeyExists("posts"),
			() -> assertForeignKeyExists("users", "raw_source"),
			() -> assertForeignKeyExists("posts", "raw_source"),
			() -> assertForeignKeyExists("posts", "users")
		);

		long rawSourceId = insertRawSource("user", 21L, "{\"id\":21}", "user-hash-21", "success");
		long userId = insertUser(rawSourceId, 21L);
		long postRawSourceId = insertRawSource("post", 21L, "{\"id\":21}", "post-hash-21", "success");
		insertPost(postRawSourceId, userId, 21L);

		assertThrows(
			DuplicateKeyException.class,
			() -> insertRawSource("user", 21L, "{\"id\":21}", "user-hash-21-dup", "success")
		);
		assertThrows(
			DuplicateKeyException.class,
			() -> insertUser(rawSourceId, 21L)
		);
		assertThrows(
			DuplicateKeyException.class,
			() -> insertPost(insertRawSource("post", 22L, "{\"id\":22}", "post-hash-22", "success"), userId, 21L)
		);
		assertThrows(
			DataIntegrityViolationException.class,
			() -> insertUser(999999L, 22L)
		);
		assertThrows(
			DataIntegrityViolationException.class,
			() -> insertPost(insertRawSource("post", 22L, "{\"id\":22}", "post-hash-22", "success"), 999999L, 22L)
		);
	}

	@Test
	@DisplayName("TC-2.5: migrations create the same schema every time")
	void migrationsCreateTheSameSchemaEveryTime() {
		Flyway flyway = Flyway.configure()
			.dataSource(dataSource)
			.load();

		assertDoesNotThrow(flyway::migrate);
		assertDoesNotThrow(flyway::migrate);

		Integer appliedMigrations = jdbcTemplate.queryForObject("""
			select count(*)
			from flyway_schema_history
			where success = true and version is not null
			""", Integer.class);

		assertEquals(3, appliedMigrations);
	}

	private long insertRawSource(String sourceType, long externalId, String rawPayload, String payloadHash, String syncResult) {
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
				values (?, ?, ?, ?, ?, ?, ?)
				""", Statement.RETURN_GENERATED_KEYS);
			statement.setString(1, sourceType);
			statement.setLong(2, externalId);
			statement.setObject(3, jsonb(rawPayload));
			statement.setString(4, payloadHash);
			statement.setTimestamp(5, Timestamp.valueOf(FIXED_TIME));
			statement.setString(6, syncResult);
			statement.setString(7, "batch-1");
			return statement;
		}, keyHolder);

		Number key = (Number) keyHolder.getKeys().get("id");
		assertNotNull(key);
		return key.longValue();
	}

	private long insertUser(long rawSourceId, long externalId) {
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
			statement.setString(3, "Leanne Graham");
			statement.setString(4, "Bret");
			statement.setString(5, "leanne@example.com");
			statement.setString(6, "1-770-736-8031 x56442");
			statement.setString(7, "hildegard.org");
			statement.setString(8, "Kulas Light");
			statement.setString(9, "Apt. 556");
			statement.setString(10, "Gwenborough");
			statement.setString(11, "92998-3874");
			statement.setString(12, "-37.3159");
			statement.setString(13, "81.1496");
			statement.setString(14, "Romaguera-Crona");
			statement.setString(15, "Multi-layered client-server neural-net");
			statement.setString(16, "harness real-time e-markets");
			statement.setTimestamp(17, Timestamp.valueOf(FIXED_TIME));
			statement.setTimestamp(18, Timestamp.valueOf(FIXED_TIME));
			return statement;
		}, keyHolder);

		Number key = (Number) keyHolder.getKeys().get("id");
		assertNotNull(key);
		return key.longValue();
	}

	private long insertPost(long rawSourceId, long userId, long externalId) {
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
			statement.setString(4, "sunt aut facere repellat provident occaecati excepturi optio reprehenderit");
			statement.setString(5, "quia et suscipit\nsuscipit recusandae consequuntur expedita et cum\nreprehenderit molestiae ut ut quas totam\nnostrum rerum est autem sunt rem eveniet architecto");
			statement.setTimestamp(6, Timestamp.valueOf(FIXED_TIME));
			statement.setTimestamp(7, Timestamp.valueOf(FIXED_TIME));
			return statement;
		}, keyHolder);

		Number key = (Number) keyHolder.getKeys().get("id");
		assertNotNull(key);
		return key.longValue();
	}

	private PGobject jsonb(String value) {
		try {
			PGobject jsonb = new PGobject();
			jsonb.setType("jsonb");
			jsonb.setValue(value);
			return jsonb;
		}
		catch (Exception ex) {
			throw new IllegalStateException("Unable to create jsonb value", ex);
		}
	}

	private void assertPrimaryKeyExists(String tableName) throws Exception {
		try (Connection connection = dataSource.getConnection()) {
			DatabaseMetaData metaData = connection.getMetaData();
			try (ResultSet resultSet = metaData.getPrimaryKeys(null, "public", tableName)) {
				assertTrue(resultSet.next(), "Expected primary key on " + tableName);
			}
		}
	}

	private void assertForeignKeyExists(String tableName, String referencedTableName) throws Exception {
		try (Connection connection = dataSource.getConnection()) {
			DatabaseMetaData metaData = connection.getMetaData();
			try (ResultSet resultSet = metaData.getImportedKeys(null, "public", tableName)) {
				boolean found = false;
				while (resultSet.next()) {
					if (referencedTableName.equalsIgnoreCase(resultSet.getString("PKTABLE_NAME"))) {
						found = true;
						break;
					}
				}
				assertTrue(found, "Expected foreign key from " + tableName + " to " + referencedTableName);
			}
		}
	}
}
