package com.example.userspostsbrowser.query;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.example.userspostsbrowser.query.dto.UserDetailResponse;
import com.example.userspostsbrowser.query.dto.UserListItemResponse;
import com.example.userspostsbrowser.query.dto.UserPostResponse;

@RestController
class UserQueryController {

	private final UserQueryService userQueryService;

	UserQueryController(UserQueryService userQueryService) {
		this.userQueryService = userQueryService;
	}

	@GetMapping("/api/users")
	List<UserListItemResponse> listUsers() {
		return userQueryService.listUsers();
	}

	@GetMapping("/api/users/{id}")
	UserDetailResponse getUser(@PathVariable long id) {
		return userQueryService.getUser(id);
	}

	@GetMapping("/api/users/{id}/posts")
	List<UserPostResponse> listPostsForUser(@PathVariable long id) {
		return userQueryService.listPostsForUser(id);
	}
}
