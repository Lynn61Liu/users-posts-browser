package com.example.userspostsbrowser.importflow;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.userspostsbrowser.importflow.dto.JsonPlaceholderPostDto;
import com.example.userspostsbrowser.importflow.dto.JsonPlaceholderUserDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class JsonPlaceholderClient {

	private final HttpClient httpClient;
	private final ObjectMapper objectMapper;
	private final URI usersUri;
	private final URI postsUri;
	private final Duration requestTimeout;

	public JsonPlaceholderClient(
		ObjectMapper objectMapper,
		@Value("${import.users-url:https://jsonplaceholder.typicode.com/users}") String usersUrl,
		@Value("${import.posts-url:https://jsonplaceholder.typicode.com/posts}") String postsUrl,
		@Value("${import.request-timeout-ms:5000}") long requestTimeoutMs
	) {
		this.objectMapper = objectMapper;
		this.usersUri = URI.create(usersUrl);
		this.postsUri = URI.create(postsUrl);
		this.requestTimeout = Duration.ofMillis(requestTimeoutMs);
		this.httpClient = HttpClient.newBuilder()
			.connectTimeout(this.requestTimeout)
			.build();
	}

	public List<JsonPlaceholderUserDto> fetchUsers() {
		return fetchList("users", usersUri, new TypeReference<List<JsonPlaceholderUserDto>>() {});
	}

	public List<JsonPlaceholderPostDto> fetchPosts() {
		return fetchList("posts", postsUri, new TypeReference<List<JsonPlaceholderPostDto>>() {});
	}

	private <T> List<T> fetchList(String label, URI uri, TypeReference<List<T>> typeReference) {
		HttpRequest request = HttpRequest.newBuilder(uri)
			.timeout(requestTimeout)
			.GET()
			.build();

		try {
			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() / 100 != 2) {
				throw new IllegalStateException(label + " request failed with status " + response.statusCode() + ": " + response.body());
			}
			return objectMapper.readValue(response.body(), typeReference);
		}
		catch (IOException ex) {
			throw new IllegalStateException(label + " response could not be parsed", ex);
		}
		catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(label + " request was interrupted", ex);
		}
	}
}
