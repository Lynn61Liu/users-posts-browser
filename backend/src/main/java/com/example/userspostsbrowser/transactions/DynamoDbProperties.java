package com.example.userspostsbrowser.transactions;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.dynamodb")
public record DynamoDbProperties(boolean enabled, String tableName, String region) {

	public DynamoDbProperties {
		tableName = tableName == null || tableName.isBlank() ? "dce042-users-posts" : tableName;
		region = region == null || region.isBlank() ? "ap-southeast-2" : region;
	}
}
