package com.example.userspostsbrowser.transactions;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.userspostsbrowser.importflow.SyncRepository;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

@Service
public class TransactionService {

	private final DynamoDbProperties properties;
	private final DynamoDbClient dynamoDbClient;

	public TransactionService(DynamoDbProperties properties, DynamoDbClient dynamoDbClient) {
		this.properties = properties;
		this.dynamoDbClient = dynamoDbClient;
	}

	public DynamoDbConfigResponse getConfig() {
		return new DynamoDbConfigResponse(properties.enabled(), properties.tableName(), properties.region(), "pk + sk");
	}

	public DemoTransactionResponse writeDemoTransaction() {
		if (!properties.enabled()) {
			return new DemoTransactionResponse(null, properties.tableName(), "disabled",
					"DynamoDB integration is disabled. Set APP_DYNAMODB_ENABLED=true to write demo transactions.");
		}

		String transactionId = "demo-" + UUID.randomUUID();
		Map<String, AttributeValue> item = new LinkedHashMap<>();
		item.put("pk", SyncRepository.stringValue("TRANSACTION#" + transactionId));
		item.put("sk", SyncRepository.stringValue("METADATA"));
		item.put("entityType", SyncRepository.stringValue("TRANSACTION"));
		item.put("transactionId", SyncRepository.stringValue(transactionId));
		item.put("serviceName", SyncRepository.stringValue("backend-service"));
		item.put("source", SyncRepository.stringValue("dce042-assessment1"));
		item.put("status", SyncRepository.stringValue("CREATED"));
		item.put("createdAt", SyncRepository.stringValue(Instant.now().toString()));

		dynamoDbClient.putItem(PutItemRequest.builder()
			.tableName(properties.tableName())
			.item(item)
			.build());

		return new DemoTransactionResponse(transactionId, properties.tableName(), "stored",
				"Demo transaction was written to DynamoDB.");
	}
}
