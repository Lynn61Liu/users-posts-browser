package com.example.userspostsbrowser.importflow;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class PayloadHashService {

	private final ObjectMapper objectMapper;

	public PayloadHashService() {
		this(new ObjectMapper());
	}

	public PayloadHashService(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public String hashJson(String json) {
		try {
			return digest(objectMapper.readTree(json).toString());
		}
		catch (JsonProcessingException ex) {
			throw new IllegalStateException("Unable to hash JSON payload", ex);
		}
	}

	public String hashObject(Object value) {
		try {
			return digest(objectMapper.writeValueAsString(value));
		}
		catch (JsonProcessingException ex) {
			throw new IllegalStateException("Unable to hash object payload", ex);
		}
	}

	private String digest(String canonicalJson) {
		try {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			byte[] hash = messageDigest.digest(canonicalJson.getBytes(StandardCharsets.UTF_8));
			StringBuilder builder = new StringBuilder();
			for (byte value : hash) {
				builder.append(String.format("%02x", value));
			}
			return builder.toString();
		}
		catch (NoSuchAlgorithmException ex) {
			throw new IllegalStateException("SHA-256 is not available", ex);
		}
	}
}
