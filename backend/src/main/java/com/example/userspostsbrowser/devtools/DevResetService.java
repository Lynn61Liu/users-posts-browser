package com.example.userspostsbrowser.devtools;

import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.example.userspostsbrowser.transactions.DynamoDbProperties;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;

@Service
class DevResetService {

	private static final Set<String> RESETTABLE_ENTITY_TYPES = Set.of("USER", "POST", "RAW_SOURCE", "TRANSACTION");

	private final DynamoDbClient dynamoDbClient;
	private final DynamoDbProperties properties;

	DevResetService(DynamoDbClient dynamoDbClient, DynamoDbProperties properties) {
		this.dynamoDbClient = dynamoDbClient;
		this.properties = properties;
	}

	DevResetResult resetDatabase() {
		int deleted = dynamoDbClient.scan(ScanRequest.builder()
			.tableName(properties.tableName())
			.projectionExpression("pk, sk, entityType")
			.build()).items()
			.stream()
			.filter(this::isResettable)
			.mapToInt(this::deleteItem)
			.sum();

		return new DevResetResult("success", "Deleted " + deleted + " DynamoDB item(s).");
	}

	private boolean isResettable(Map<String, AttributeValue> item) {
		AttributeValue entityType = item.get("entityType");
		return entityType != null && RESETTABLE_ENTITY_TYPES.contains(entityType.s());
	}

	private int deleteItem(Map<String, AttributeValue> item) {
		dynamoDbClient.deleteItem(DeleteItemRequest.builder()
			.tableName(properties.tableName())
			.key(Map.of("pk", item.get("pk"), "sk", item.get("sk")))
			.build());
		return 1;
	}
}
