package com.example.userspostsbrowser.query;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.userspostsbrowser.query.dto.ApiErrorResponse;

@RestControllerAdvice
class QueryApiExceptionHandler {

	@ExceptionHandler(UserNotFoundException.class)
	ResponseEntity<ApiErrorResponse> handleUserNotFound(UserNotFoundException ex, HttpServletRequest request) {
		return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
	}

	private ResponseEntity<ApiErrorResponse> buildErrorResponse(HttpStatus status, String message, String path) {
		return ResponseEntity.status(status).body(new ApiErrorResponse(
			status.value(),
			status.getReasonPhrase(),
			message,
			path
		));
	}
}
