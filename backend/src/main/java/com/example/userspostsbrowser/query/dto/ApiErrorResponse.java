package com.example.userspostsbrowser.query.dto;

public record ApiErrorResponse(
	int status,
	String error,
	String message,
	String path
) {
}
