package com.example.userspostsbrowser.query;

class UserNotFoundException extends RuntimeException {

	UserNotFoundException(long userId) {
		super("User " + userId + " was not found");
	}
}
