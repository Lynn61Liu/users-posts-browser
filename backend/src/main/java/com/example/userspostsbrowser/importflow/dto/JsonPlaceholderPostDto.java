package com.example.userspostsbrowser.importflow.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record JsonPlaceholderPostDto(
	long userId,
	long id,
	String title,
	String body
) {
}
