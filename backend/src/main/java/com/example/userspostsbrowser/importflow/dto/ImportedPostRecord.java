package com.example.userspostsbrowser.importflow.dto;

public record ImportedPostRecord(
	long externalId,
	long userExternalId,
	String title,
	String body
) {
}
