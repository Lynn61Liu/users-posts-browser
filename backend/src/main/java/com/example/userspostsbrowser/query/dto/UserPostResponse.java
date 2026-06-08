package com.example.userspostsbrowser.query.dto;

public record UserPostResponse(
	long id,
	long externalId,
	String title,
	String body
) {
}
