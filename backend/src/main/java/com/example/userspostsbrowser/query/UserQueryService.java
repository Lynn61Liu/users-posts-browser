package com.example.userspostsbrowser.query;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.userspostsbrowser.query.dto.UserDetailResponse;
import com.example.userspostsbrowser.query.dto.UserListItemResponse;
import com.example.userspostsbrowser.query.dto.UserPostResponse;

@Service
class UserQueryService {

	private final UserQueryRepository userQueryRepository;

	UserQueryService(UserQueryRepository userQueryRepository) {
		this.userQueryRepository = userQueryRepository;
	}

	List<UserListItemResponse> listUsers() {
		return userQueryRepository.findAllUsers();
	}

	UserDetailResponse getUser(long userId) {
		return userQueryRepository.findUserById(userId)
			.orElseThrow(() -> new UserNotFoundException(userId));
	}

	List<UserPostResponse> listPostsForUser(long userId) {
		userQueryRepository.findUserById(userId)
			.orElseThrow(() -> new UserNotFoundException(userId));
		return userQueryRepository.findPostsByUserId(userId);
	}
}
