package com.example.userspostsbrowser.transactions;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@Configuration
@EnableConfigurationProperties(DynamoDbProperties.class)
class DynamoDbConfiguration {

	@Bean
	DynamoDbClient dynamoDbClient(DynamoDbProperties properties) {
		return DynamoDbClient.builder()
			.region(Region.of(properties.region()))
			.build();
	}
}
