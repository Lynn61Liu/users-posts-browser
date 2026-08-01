package com.example.userspostsbrowser.transactions;

public record DynamoDbConfigResponse(boolean enabled, String tableName, String region, String keySchema) {
}
