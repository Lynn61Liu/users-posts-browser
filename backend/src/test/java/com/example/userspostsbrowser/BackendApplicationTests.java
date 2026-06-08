package com.example.userspostsbrowser;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
class BackendApplicationTests {

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

	@Test
	@DisplayName("TC-1.1: Backend project starts successfully")
	void backendProjectStartsSuccessfully() {
		// If the Spring context cannot boot, this test class fails before this method runs.
	}

	@Test
	void databaseSchemaHasUsersPostsAndRawSourceTables() throws Exception {
		try (Connection connection = dataSource.getConnection()) {
			DatabaseMetaData metaData = connection.getMetaData();

			assertTrue(tableExists(metaData, "users"));
			assertTrue(tableExists(metaData, "posts"));
			assertTrue(tableExists(metaData, "raw_source"));
		}
	}

	private boolean tableExists(DatabaseMetaData metaData, String tableName) throws Exception {
		try (ResultSet resultSet = metaData.getTables(null, "public", tableName, new String[] {"TABLE"})) {
			return resultSet.next();
		}
	}

}
