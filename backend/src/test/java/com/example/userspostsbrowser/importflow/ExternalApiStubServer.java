package com.example.userspostsbrowser.importflow;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

final class ExternalApiStubServer implements AutoCloseable {

	private final HttpServer server;
	private final AtomicReference<String> usersBody = new AtomicReference<>("[]");
	private final AtomicReference<String> postsBody = new AtomicReference<>("[]");
	private final AtomicReference<Integer> usersStatus = new AtomicReference<>(200);
	private final AtomicReference<Integer> postsStatus = new AtomicReference<>(200);

	private ExternalApiStubServer(HttpServer server) {
		this.server = server;
	}

	static ExternalApiStubServer start() {
		try {
			HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
			ExternalApiStubServer stub = new ExternalApiStubServer(server);
			server.createContext("/users", new JsonResponseHandler(stub.usersStatus, stub.usersBody));
			server.createContext("/posts", new JsonResponseHandler(stub.postsStatus, stub.postsBody));
			server.start();
			return stub;
		}
		catch (IOException ex) {
			throw new IllegalStateException("Unable to start stub server", ex);
		}
	}

	void setUsersResponse(int status, String body) {
		usersStatus.set(status);
		usersBody.set(body);
	}

	void setPostsResponse(int status, String body) {
		postsStatus.set(status);
		postsBody.set(body);
	}

	URI usersUri() {
		return URI.create(baseUrl() + "/users");
	}

	URI postsUri() {
		return URI.create(baseUrl() + "/posts");
	}

	String baseUrl() {
		return "http://localhost:" + server.getAddress().getPort();
	}

	@Override
	public void close() {
		server.stop(0);
	}

	private static final class JsonResponseHandler implements HttpHandler {

		private final AtomicReference<Integer> status;
		private final AtomicReference<String> body;

		private JsonResponseHandler(AtomicReference<Integer> status, AtomicReference<String> body) {
			this.status = status;
			this.body = body;
		}

		@Override
		public void handle(HttpExchange exchange) throws IOException {
			byte[] responseBytes = body.get().getBytes(StandardCharsets.UTF_8);
			exchange.getResponseHeaders().add("Content-Type", "application/json");
			exchange.sendResponseHeaders(status.get(), responseBytes.length);
			try (OutputStream outputStream = exchange.getResponseBody()) {
				outputStream.write(responseBytes);
			}
		}
	}
}
