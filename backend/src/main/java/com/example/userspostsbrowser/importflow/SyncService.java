package com.example.userspostsbrowser.importflow;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.userspostsbrowser.importflow.dto.ImportedPostRecord;
import com.example.userspostsbrowser.importflow.dto.ImportedUserRecord;
import com.example.userspostsbrowser.importflow.dto.JsonPlaceholderPostDto;
import com.example.userspostsbrowser.importflow.dto.JsonPlaceholderUserDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class SyncService {

	private final JsonPlaceholderClient jsonPlaceholderClient;
	private final UserMapper userMapper;
	private final PostMapper postMapper;
	private final PayloadHashService payloadHashService;
	private final SyncRepository syncRepository;
	private final ObjectMapper objectMapper;

	public SyncService(
		JsonPlaceholderClient jsonPlaceholderClient,
		UserMapper userMapper,
		PostMapper postMapper,
		PayloadHashService payloadHashService,
		SyncRepository syncRepository,
		ObjectMapper objectMapper
	) {
		this.jsonPlaceholderClient = jsonPlaceholderClient;
		this.userMapper = userMapper;
		this.postMapper = postMapper;
		this.payloadHashService = payloadHashService;
		this.syncRepository = syncRepository;
		this.objectMapper = objectMapper;
	}

	public SyncResult sync() {
		try {
			List<JsonPlaceholderUserDto> users = jsonPlaceholderClient.fetchUsers();
			List<JsonPlaceholderPostDto> posts = jsonPlaceholderClient.fetchPosts();
			return syncRecords(users, posts);
		}
		catch (Exception ex) {
			return SyncResult.error(resolveErrorMessage(ex));
		}
	}

	static String resolveErrorMessage(Throwable throwable) {
		String message = firstMeaningfulMessage(throwable);
		if (message == null || message.isBlank()) {
			return "Sync failed. Check the backend logs and try again.";
		}

		return message;
	}

	private SyncResult syncRecords(List<JsonPlaceholderUserDto> users, List<JsonPlaceholderPostDto> posts) {
		Instant now = Instant.now();
		String batchId = UUID.randomUUID().toString();
		int changedUsers = 0;
		int changedPosts = 0;
		int rawRecordsProcessed = 0;
		boolean updatedExistingRecords = false;
		Map<Long, Boolean> usersByExternalId = new HashMap<>();

		for (JsonPlaceholderUserDto userDto : users) {
			ImportedUserRecord user = userMapper.toImportedUserRecord(userDto);
			String rawPayload = serialize(userDto);
			String payloadHash = payloadHashService.hashObject(userDto);
			Optional<RawSourceSnapshot> existing = syncRepository.findRawSource("user", user.externalId());

			if (existing.isEmpty()) {
				syncRepository.putRawSource("user", user.externalId(), rawPayload, payloadHash, now, "success", batchId);
				syncRepository.upsertUser(user, now);
				changedUsers++;
			}
			else if (existing.get().payloadHash().equals(payloadHash)) {
				syncRepository.putRawSource("user", user.externalId(), rawPayload, payloadHash, now, "no_change", batchId);
			}
			else {
				syncRepository.putRawSource("user", user.externalId(), rawPayload, payloadHash, now, "update", batchId);
				syncRepository.upsertUser(user, now);
				changedUsers++;
				updatedExistingRecords = true;
			}

			usersByExternalId.put(user.externalId(), true);
			rawRecordsProcessed++;
		}

		for (JsonPlaceholderPostDto postDto : posts) {
			ImportedPostRecord post = postMapper.toImportedPostRecord(postDto);
			String rawPayload = serialize(postDto);
			String payloadHash = payloadHashService.hashObject(postDto);
			if (!usersByExternalId.containsKey(post.userExternalId()) && !syncRepository.userExists(post.userExternalId())) {
				throw new IllegalStateException("User " + post.userExternalId() + " was not found");
			}
			Optional<RawSourceSnapshot> existing = syncRepository.findRawSource("post", post.externalId());

			if (existing.isEmpty()) {
				syncRepository.putRawSource("post", post.externalId(), rawPayload, payloadHash, now, "success", batchId);
				syncRepository.upsertPost(post, now);
				changedPosts++;
			}
			else if (existing.get().payloadHash().equals(payloadHash)) {
				syncRepository.putRawSource("post", post.externalId(), rawPayload, payloadHash, now, "no_change", batchId);
			}
			else {
				syncRepository.putRawSource("post", post.externalId(), rawPayload, payloadHash, now, "update", batchId);
				syncRepository.upsertPost(post, now);
				changedPosts++;
				updatedExistingRecords = true;
			}

			rawRecordsProcessed++;
		}

		if (changedUsers == 0 && changedPosts == 0) {
			return SyncResult.noChange("No changes detected.", 0, 0, rawRecordsProcessed);
		}
		if (updatedExistingRecords) {
			return SyncResult.update(
				"Updated " + changedUsers + " user" + pluralSuffix(changedUsers)
					+ " and " + changedPosts + " post" + pluralSuffix(changedPosts) + ".",
				changedUsers,
				changedPosts,
				rawRecordsProcessed
			);
		}
		return SyncResult.success(
			"Imported " + changedUsers + " user" + pluralSuffix(changedUsers)
				+ " and " + changedPosts + " post" + pluralSuffix(changedPosts) + ".",
			changedUsers,
			changedPosts,
			rawRecordsProcessed
		);
	}

	private String serialize(Object value) {
		try {
			return objectMapper.writeValueAsString(value);
		}
		catch (JsonProcessingException ex) {
			throw new IllegalStateException("Unable to serialize imported payload", ex);
		}
	}

	private String pluralSuffix(int count) {
		return count == 1 ? "" : "s";
	}

	private static String firstMeaningfulMessage(Throwable throwable) {
		Throwable current = throwable;
		while (current != null) {
			String message = current.getMessage();
			if (message != null && !message.isBlank()) {
				return message;
			}
			current = current.getCause();
		}
		return null;
	}
}
