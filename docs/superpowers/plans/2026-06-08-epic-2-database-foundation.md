# Epic 2: Database Foundation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Create the PostgreSQL schema for users, posts, and raw source traceability so import and query flows have a stable database foundation.

**Architecture:** Use Spring Boot's Flyway support plus versioned SQL migrations under the backend project so the schema is created automatically on application startup. Keep the schema focused on the three tables from `MySolution.MD`: normalized `users` and `posts` tables plus a `raw_source` traceability table. Verify the schema with a Spring Boot test that inspects database metadata against a PostgreSQL Testcontainers instance, so the migration contract is covered without adding application logic yet.

**Tech Stack:** Spring Boot, PostgreSQL, Flyway, Maven, JUnit 5, Testcontainers for test-time database execution.

---

### Task 1: Add schema verification coverage

**Files:**
- Modify: `backend/src/test/java/com/example/userspostsbrowser/BackendApplicationTests.java`
- Modify: `backend/src/test/resources/application.properties`
- Modify: `backend/pom.xml`

- [ ] **Step 1: Write the failing test**

```java
@SpringBootTest
class BackendApplicationTests {

	@Autowired
	DataSource dataSource;

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
		try (ResultSet resultSet = metaData.getTables(null, null, tableName, new String[] {"TABLE"})) {
			return resultSet.next();
		}
	}
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `cd backend && ./mvnw test -Dtest=BackendApplicationTests#databaseSchemaHasUsersPostsAndRawSourceTables`

Expected: FAIL because Flyway migrations are not present yet and the schema tables do not exist.

- [ ] **Step 3: Keep the test harness isolated to the containerized PostgreSQL database**

```properties
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=none
spring.jpa.open-in-view=false
```

- [ ] **Step 4: Re-run the test after migrations are added**

Run: `cd backend && ./mvnw test -Dtest=BackendApplicationTests#databaseSchemaHasUsersPostsAndRawSourceTables`

Expected: PASS once Flyway creates the tables.

### Task 2: Add Flyway migrations for the schema

**Files:**
- Modify: `backend/pom.xml`
- Create: `backend/src/main/resources/db/migration/V1__create_raw_source.sql`
- Create: `backend/src/main/resources/db/migration/V2__create_users.sql`
- Create: `backend/src/main/resources/db/migration/V3__create_posts.sql`

- [ ] **Step 1: Add Flyway to the backend build**

```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-flyway</artifactId>
</dependency>
<dependency>
	<groupId>org.flywaydb</groupId>
	<artifactId>flyway-database-postgresql</artifactId>
</dependency>
```

- [ ] **Step 2: Create the raw source migration**

```sql
create table raw_source (
    id bigserial primary key,
    source_type varchar(20) not null,
    external_id bigint not null,
    raw_payload jsonb not null,
    payload_hash varchar(64) not null,
    synced_at timestamp not null,
    sync_result varchar(20) not null,
    sync_batch_id varchar(64),
    constraint uq_raw_source_source_type_external_id unique (source_type, external_id)
);
```

- [ ] **Step 3: Create the users migration**

```sql
create table users (
    id bigserial primary key,
    external_id bigint not null,
    raw_source_id bigint not null,
    name varchar(255) not null,
    username varchar(255) not null,
    email varchar(255) not null,
    phone varchar(100),
    website varchar(255),
    address_street varchar(255),
    address_suite varchar(255),
    address_city varchar(255),
    address_zipcode varchar(50),
    address_geo_lat varchar(50),
    address_geo_lng varchar(50),
    company_name varchar(255),
    company_catch_phrase varchar(255),
    company_bs varchar(255),
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint uq_users_external_id unique (external_id),
    constraint fk_users_raw_source foreign key (raw_source_id) references raw_source (id)
);
```

- [ ] **Step 4: Create the posts migration**

```sql
create table posts (
    id bigserial primary key,
    external_id bigint not null,
    raw_source_id bigint not null,
    user_id bigint not null,
    title varchar(255) not null,
    body text not null,
    created_at timestamp not null,
    updated_at timestamp not null,
    constraint uq_posts_external_id unique (external_id),
    constraint fk_posts_raw_source foreign key (raw_source_id) references raw_source (id),
    constraint fk_posts_user foreign key (user_id) references users (id)
);
```

- [ ] **Step 5: Run the backend test suite**

Run: `cd backend && ./mvnw test`

Expected: PASS, with Flyway applying the migrations before the Spring Boot test starts.

### Task 3: Verify the containerized schema startup

**Files:**
- Modify: `docker-compose.yml` only if startup ordering or env wiring needs adjustment

- [ ] **Step 1: Rebuild the backend image**

Run: `docker compose build backend`

Expected: PASS.

- [ ] **Step 2: Start the database stack**

Run: `docker compose up -d postgres backend`

Expected: both services start, and backend should start successfully after PostgreSQL becomes healthy.

- [ ] **Step 3: Confirm the schema is present**

Run: `docker compose exec postgres psql -U users_posts_browser -d users_posts_browser -c "\dt"`

Expected: list contains `raw_source`, `users`, and `posts`.
