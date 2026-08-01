package com.example.userspostsbrowser.transactions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;

class TransactionServiceTests {

	@Test
	@DisplayName("Demo transaction writes a single-table item with pk and sk")
	void demoTransactionWritesSingleTableItemWithPkAndSk() {
		DynamoDbClient dynamoDbClient = mock(DynamoDbClient.class);
		when(dynamoDbClient.putItem(org.mockito.ArgumentMatchers.any(PutItemRequest.class)))
			.thenReturn(PutItemResponse.builder().build());
		TransactionService service = new TransactionService(
				new DynamoDbProperties(true, "dce042-users-posts", "ap-southeast-2"), dynamoDbClient);

		DemoTransactionResponse response = service.writeDemoTransaction();

		ArgumentCaptor<PutItemRequest> captor = ArgumentCaptor.forClass(PutItemRequest.class);
		verify(dynamoDbClient).putItem(captor.capture());
		Map<String, AttributeValue> item = captor.getValue().item();

		assertEquals("stored", response.status());
		assertEquals("dce042-users-posts", response.tableName());
		assertNotNull(response.transactionId());
		assertEquals("METADATA", item.get("sk").s());
		assertEquals("TRANSACTION", item.get("entityType").s());
		assertEquals(response.transactionId(), item.get("transactionId").s());
	}
}
