package com.example.userspostsbrowser.query.dto;

public record UserListItemResponse(
	long id,
	long externalId,
	String name,
	String username,
	String email,
	String companyName
) {
}
