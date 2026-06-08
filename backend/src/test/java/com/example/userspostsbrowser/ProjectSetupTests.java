package com.example.userspostsbrowser;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProjectSetupTests {

	private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
		.connectTimeout(Duration.ofSeconds(2))
		.build();

	@Test
	@DisplayName("TC-1.3: Docker Compose starts all services")
	void dockerComposeStartsAllServices() throws Exception {
		runDockerComposeUp();

		HttpResponse<String> backendHealth = waitForHttpResponse("http://localhost:8080/actuator/health");
		assertEquals(200, backendHealth.statusCode());
		assertTrue(backendHealth.body().contains("\"status\":\"UP\""));

		HttpResponse<String> frontendHome = waitForHttpResponse("http://localhost:3000/");
		assertEquals(200, frontendHome.statusCode());
		assertTrue(frontendHome.body().contains("<div id=\"root\"></div>"));
	}

	@Test
	@DisplayName("TC-1.5: README instructions are usable")
	void readmeInstructionsAreUsable() throws Exception {
		String readme = Files.readString(Path.of("..", "README.md"), StandardCharsets.UTF_8);

		assertAll(
			() -> assertTrue(readme.contains("docker compose up --build")),
			() -> assertTrue(readme.contains("Copy `.env.example` to `.env`")),
			() -> assertTrue(readme.contains("Frontend: http://localhost:3000")),
			() -> assertTrue(readme.contains("Backend: http://localhost:8080")),
			() -> assertTrue(readme.contains("PostgreSQL: localhost:5432"))
		);
	}

	private static void runDockerComposeUp() throws Exception {
		Process process = new ProcessBuilder("docker", "compose", "up", "-d", "--wait")
			.directory(Path.of("..").toFile())
			.redirectErrorStream(true)
			.start();

		String output = readProcessOutput(process);
		int exitCode = process.waitFor();

		if (exitCode != 0) {
			fail("docker compose up failed with exit code " + exitCode + ":\n" + output);
		}
	}

	private static String readProcessOutput(Process process) throws IOException {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
			StringBuilder builder = new StringBuilder();
			String line;

			while ((line = reader.readLine()) != null) {
				builder.append(line).append(System.lineSeparator());
			}

			return builder.toString();
		}
	}

	private static HttpResponse<String> waitForHttpResponse(String url) throws Exception {
		Exception lastFailure = null;

		for (int attempt = 0; attempt < 60; attempt++) {
			try {
				HttpRequest request = HttpRequest.newBuilder(URI.create(url))
					.timeout(Duration.ofSeconds(2))
					.GET()
					.build();

				HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
				if (response.statusCode() == 200) {
					return response;
				}
			}
			catch (Exception ex) {
				lastFailure = ex;
			}

			Thread.sleep(1000);
		}

		throw new AssertionError("Timed out waiting for " + url, lastFailure);
	}
}
