package com.example.userspostsbrowser.importflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.userspostsbrowser.importflow.dto.JsonPlaceholderPostDto;
import com.example.userspostsbrowser.importflow.dto.JsonPlaceholderUserDto;
import com.fasterxml.jackson.databind.ObjectMapper;

class JsonPlaceholderClientTests {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private static ExternalApiStubServer stubServer;

	@BeforeAll
	static void setUpServer() {
		stubServer = ExternalApiStubServer.start();
	}

	@AfterAll
	static void tearDownServer() {
		stubServer.close();
	}

	@Test
	@DisplayName("TC-3.1: Users endpoint response is parsed correctly")
	void usersEndpointResponseIsParsedCorrectly() {
		stubServer.setUsersResponse(200, """
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
			""");
		stubServer.setPostsResponse(200, "[]");

		JsonPlaceholderClient client = new JsonPlaceholderClient(
			OBJECT_MAPPER,
			stubServer.usersUri().toString(),
			stubServer.postsUri().toString(),
			Duration.ofSeconds(2).toMillis()
		);

		List<JsonPlaceholderUserDto> users = client.fetchUsers();

		assertEquals(1, users.size());
		assertEquals(1L, users.get(0).id());
		assertEquals("Leanne Graham", users.get(0).name());
		assertEquals("Gwenborough", users.get(0).address().city());
		assertEquals("Romaguera-Crona", users.get(0).company().name());
	}

	@Test
	@DisplayName("TC-3.2: Posts endpoint response is parsed correctly")
	void postsEndpointResponseIsParsedCorrectly() {
		stubServer.setUsersResponse(200, "[]");
		stubServer.setPostsResponse(200, """
			[
			  {
			    "userId": 1,
			    "id": 1,
			    "title": "sunt aut facere repellat provident occaecati excepturi optio reprehenderit",
			    "body": "quia et suscipit"
			  }
			]
			""");

		JsonPlaceholderClient client = new JsonPlaceholderClient(
			OBJECT_MAPPER,
			stubServer.usersUri().toString(),
			stubServer.postsUri().toString(),
			Duration.ofSeconds(2).toMillis()
		);

		List<JsonPlaceholderPostDto> posts = client.fetchPosts();

		assertEquals(1, posts.size());
		assertEquals(1L, posts.get(0).userId());
		assertEquals(1L, posts.get(0).id());
		assertEquals("sunt aut facere repellat provident occaecati excepturi optio reprehenderit", posts.get(0).title());
	}

	@Test
	@DisplayName("TC-3.1 and TC-3.2: external API failures are handled clearly")
	void externalApiFailuresAreHandledClearly() {
		stubServer.setUsersResponse(500, "{\"error\":\"boom\"}");
		stubServer.setPostsResponse(500, "{\"error\":\"boom\"}");

		JsonPlaceholderClient client = new JsonPlaceholderClient(
			OBJECT_MAPPER,
			stubServer.usersUri().toString(),
			stubServer.postsUri().toString(),
			Duration.ofSeconds(2).toMillis()
		);

		assertThrows(IllegalStateException.class, client::fetchUsers);
		assertThrows(IllegalStateException.class, client::fetchPosts);
	}
}
