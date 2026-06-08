package com.example.userspostsbrowser.importflow;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

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
	private final TransactionTemplate transactionTemplate;
	private final ObjectMapper objectMapper;

	public SyncService(
		JsonPlaceholderClient jsonPlaceholderClient,
		UserMapper userMapper,
		PostMapper postMapper,
		PayloadHashService payloadHashService,
		SyncRepository syncRepository,
		PlatformTransactionManager transactionManager,
		ObjectMapper objectMapper
	) {
		this.jsonPlaceholderClient = jsonPlaceholderClient;
		this.userMapper = userMapper;
		this.postMapper = postMapper;
		this.payloadHashService = payloadHashService;
		this.syncRepository = syncRepository;
		this.transactionTemplate = new TransactionTemplate(transactionManager);
		this.objectMapper = objectMapper;
	}

	public SyncResult sync() {
		try {
			List<JsonPlaceholderUserDto> users = jsonPlaceholderClient.fetchUsers();
			List<JsonPlaceholderPostDto> posts = jsonPlaceholderClient.fetchPosts();
			SyncResult result = transactionTemplate.execute(status -> syncWithinTransaction(users, posts));
			return result == null ? SyncResult.error("Sync did not return a result") : result;
		}
		catch (Exception ex) {
			return SyncResult.error(resolveErrorMessage(ex));
		}
	}

	static String resolveErrorMessage(Throwable throwable) {
		if (isDatabaseUnavailable(throwable)) {
			return "Could not reach PostgreSQL. Make sure the database is running, then try again.";
		}

		String message = firstMeaningfulMessage(throwable);
		if (message == null || message.isBlank()) {
			return "Sync failed. Check the backend logs and try again.";
		}

		return message;
	}

	private SyncResult syncWithinTransaction(List<JsonPlaceholderUserDto> users, List<JsonPlaceholderPostDto> posts) {
		Instant now = Instant.now();
		String batchId = UUID.randomUUID().toString();
		int changedUsers = 0;
		int changedPosts = 0;
		int rawRecordsProcessed = 0;
		boolean updatedExistingRecords = false;
		Map<Long, Long> userIdsByExternalId = new HashMap<>();

		for (JsonPlaceholderUserDto userDto : users) {
			ImportedUserRecord user = userMapper.toImportedUserRecord(userDto);
			String rawPayload = serialize(userDto);
			String payloadHash = payloadHashService.hashObject(userDto);
			Optional<RawSourceSnapshot> existing = syncRepository.findRawSource("user", user.externalId());

			long userId;
			if (existing.isEmpty()) {
				long rawSourceId = syncRepository.insertRawSource("user", user.externalId(), rawPayload, payloadHash, now, "success", batchId);
				userId = syncRepository.upsertUser(user, rawSourceId, now);
				changedUsers++;
			}
			else if (existing.get().payloadHash().equals(payloadHash)) {
				syncRepository.updateRawSource(existing.get().id(), rawPayload, payloadHash, now, "no_change", batchId);
				userId = syncRepository.findUserIdByExternalId(user.externalId());
			}
			else {
				syncRepository.updateRawSource(existing.get().id(), rawPayload, payloadHash, now, "update", batchId);
				userId = syncRepository.upsertUser(user, existing.get().id(), now);
				changedUsers++;
				updatedExistingRecords = true;
			}

			userIdsByExternalId.put(user.externalId(), userId);
			rawRecordsProcessed++;
		}

		for (JsonPlaceholderPostDto postDto : posts) {
			ImportedPostRecord post = postMapper.toImportedPostRecord(postDto);
			String rawPayload = serialize(postDto);
			String payloadHash = payloadHashService.hashObject(postDto);
			Long mappedUserId = userIdsByExternalId.get(post.userExternalId());
			long userId = mappedUserId != null ? mappedUserId : syncRepository.findUserIdByExternalId(post.userExternalId());
			Optional<RawSourceSnapshot> existing = syncRepository.findRawSource("post", post.externalId());

			if (existing.isEmpty()) {
				long rawSourceId = syncRepository.insertRawSource("post", post.externalId(), rawPayload, payloadHash, now, "success", batchId);
				syncRepository.upsertPost(post, rawSourceId, userId, now);
				changedPosts++;
			}
			else if (existing.get().payloadHash().equals(payloadHash)) {
				syncRepository.updateRawSource(existing.get().id(), rawPayload, payloadHash, now, "no_change", batchId);
			}
			else {
				syncRepository.updateRawSource(existing.get().id(), rawPayload, payloadHash, now, "update", batchId);
				syncRepository.upsertPost(post, existing.get().id(), userId, now);
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

	private static boolean isDatabaseUnavailable(Throwable throwable) {
		Throwable current = throwable;
		while (current != null) {
			String message = normalize(current.getMessage());
			String type = current.getClass().getSimpleName().toLowerCase();

			if (message.contains("could not open jpa entitymanager for transaction")
				|| message.contains("failed to obtain jdbc connection")
				|| message.contains("unable to acquire jdbc connection")
				|| message.contains("connection refused")
				|| message.contains("connection is not available")
				|| message.contains("communications link failure")
				|| message.contains("connection timed out")
				|| type.contains("cannotgetjdbcconnectionexception")
				|| type.contains("sqltransientconnectionexception")) {
				return true;
			}

			current = current.getCause();
		}

		return false;
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

	private static String normalize(String value) {
		return value == null ? "" : value.toLowerCase();
	}
}
