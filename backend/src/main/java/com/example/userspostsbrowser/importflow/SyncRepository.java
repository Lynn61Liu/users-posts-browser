package com.example.userspostsbrowser.importflow;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.example.userspostsbrowser.importflow.dto.ImportedPostRecord;
import com.example.userspostsbrowser.importflow.dto.ImportedUserRecord;
import com.example.userspostsbrowser.transactions.DynamoDbProperties;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

@Repository
public class SyncRepository {

	private final DynamoDbClient dynamoDbClient;
	private final DynamoDbProperties properties;

	public SyncRepository(DynamoDbClient dynamoDbClient, DynamoDbProperties properties) {
		this.dynamoDbClient = dynamoDbClient;
		this.properties = properties;
	}

	public Optional<RawSourceSnapshot> findRawSource(String sourceType, long externalId) {
		Map<String, AttributeValue> item = dynamoDbClient.getItem(GetItemRequest.builder()
			.tableName(properties.tableName())
			.key(key(rawPk(sourceType, externalId), "METADATA"))
			.build()).item();

		if (item == null || item.isEmpty()) {
			return Optional.empty();
		}

		return Optional.of(new RawSourceSnapshot(sourceType, externalId, string(item, "payloadHash")));
	}

	public void putRawSource(String sourceType, long externalId, String rawPayload, String payloadHash, Instant syncedAt, String syncResult, String syncBatchId) {
		Map<String, AttributeValue> item = new LinkedHashMap<>();
		item.put("pk", stringValue(rawPk(sourceType, externalId)));
		item.put("sk", stringValue("METADATA"));
		item.put("entityType", stringValue("RAW_SOURCE"));
		item.put("sourceType", stringValue(sourceType));
		item.put("externalId", numberValue(externalId));
		item.put("rawPayload", stringValue(rawPayload));
		item.put("payloadHash", stringValue(payloadHash));
		item.put("syncedAt", stringValue(syncedAt.toString()));
		item.put("syncResult", stringValue(syncResult));
		item.put("syncBatchId", stringValue(syncBatchId));
		putItem(item);
	}

	public void upsertUser(ImportedUserRecord user, Instant now) {
		Map<String, AttributeValue> item = new LinkedHashMap<>();
		item.put("pk", stringValue(userPk(user.externalId())));
		item.put("sk", stringValue("PROFILE"));
		item.put("entityType", stringValue("USER"));
		item.put("id", numberValue(user.externalId()));
		item.put("externalId", numberValue(user.externalId()));
		item.put("name", stringValue(user.name()));
		item.put("username", stringValue(user.username()));
		item.put("email", stringValue(user.email()));
		item.put("phone", stringValue(user.phone()));
		item.put("website", stringValue(user.website()));
		item.put("addressStreet", stringValue(user.addressStreet()));
		item.put("addressSuite", stringValue(user.addressSuite()));
		item.put("addressCity", stringValue(user.addressCity()));
		item.put("addressZipcode", stringValue(user.addressZipcode()));
		item.put("addressGeoLat", stringValue(user.addressGeoLat()));
		item.put("addressGeoLng", stringValue(user.addressGeoLng()));
		item.put("companyName", stringValue(user.companyName()));
		item.put("companyCatchPhrase", stringValue(user.companyCatchPhrase()));
		item.put("companyBs", stringValue(user.companyBs()));
		item.put("updatedAt", stringValue(now.toString()));
		putItem(item);
	}

	public void upsertPost(ImportedPostRecord post, Instant now) {
		Map<String, AttributeValue> item = new LinkedHashMap<>();
		item.put("pk", stringValue(userPk(post.userExternalId())));
		item.put("sk", stringValue(postSk(post.externalId())));
		item.put("entityType", stringValue("POST"));
		item.put("id", numberValue(post.externalId()));
		item.put("externalId", numberValue(post.externalId()));
		item.put("userExternalId", numberValue(post.userExternalId()));
		item.put("title", stringValue(post.title()));
		item.put("body", stringValue(post.body()));
		item.put("updatedAt", stringValue(now.toString()));
		putItem(item);
	}

	public boolean userExists(long externalId) {
		Map<String, AttributeValue> item = dynamoDbClient.getItem(GetItemRequest.builder()
			.tableName(properties.tableName())
			.key(key(userPk(externalId), "PROFILE"))
			.build()).item();
		return item != null && !item.isEmpty();
	}

	private void putItem(Map<String, AttributeValue> item) {
		dynamoDbClient.putItem(PutItemRequest.builder()
			.tableName(properties.tableName())
			.item(item)
			.build());
	}

	public static Map<String, AttributeValue> key(String pk, String sk) {
		Map<String, AttributeValue> key = new LinkedHashMap<>();
		key.put("pk", stringValue(pk));
		key.put("sk", stringValue(sk));
		return key;
	}

	public static String userPk(long externalId) {
		return "USER#" + externalId;
	}

	public static String postSk(long externalId) {
		return "POST#" + externalId;
	}

	static String rawPk(String sourceType, long externalId) {
		return "RAW#" + sourceType + "#" + externalId;
	}

	public static AttributeValue stringValue(String value) {
		return AttributeValue.builder()
			.s(value == null ? "" : value)
			.build();
	}

	static AttributeValue numberValue(long value) {
		return AttributeValue.builder()
			.n(Long.toString(value))
			.build();
	}

	public static String string(Map<String, AttributeValue> item, String name) {
		AttributeValue value = item.get(name);
		return value == null ? "" : value.s();
	}

	public static long number(Map<String, AttributeValue> item, String name) {
		AttributeValue value = item.get(name);
		return value == null ? 0L : Long.parseLong(value.n());
	}
}

record RawSourceSnapshot(String sourceType, long externalId, String payloadHash) {
}
