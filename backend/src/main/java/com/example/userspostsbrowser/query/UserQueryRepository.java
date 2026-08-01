package com.example.userspostsbrowser.query;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.example.userspostsbrowser.importflow.SyncRepository;
import com.example.userspostsbrowser.query.dto.UserDetailResponse;
import com.example.userspostsbrowser.query.dto.UserListItemResponse;
import com.example.userspostsbrowser.query.dto.UserPostResponse;
import com.example.userspostsbrowser.transactions.DynamoDbProperties;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;

@Repository
class UserQueryRepository {

	private final DynamoDbClient dynamoDbClient;
	private final DynamoDbProperties properties;

	UserQueryRepository(DynamoDbClient dynamoDbClient, DynamoDbProperties properties) {
		this.dynamoDbClient = dynamoDbClient;
		this.properties = properties;
	}

	List<UserListItemResponse> findAllUsers() {
		return dynamoDbClient.scan(ScanRequest.builder()
			.tableName(properties.tableName())
			.filterExpression("entityType = :entityType")
			.expressionAttributeValues(Map.of(":entityType", SyncRepository.stringValue("USER")))
			.build()).items()
			.stream()
			.map(this::mapUserListItem)
			.sorted(Comparator.comparingLong(UserListItemResponse::id))
			.toList();
	}

	Optional<UserDetailResponse> findUserById(long userId) {
		Map<String, AttributeValue> item = dynamoDbClient.getItem(GetItemRequest.builder()
			.tableName(properties.tableName())
			.key(SyncRepository.key(SyncRepository.userPk(userId), "PROFILE"))
			.build()).item();
		if (item == null || item.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(mapUserDetail(item));
	}

	List<UserPostResponse> findPostsByUserId(long userId) {
		return dynamoDbClient.query(QueryRequest.builder()
			.tableName(properties.tableName())
			.keyConditionExpression("pk = :pk and begins_with(sk, :postPrefix)")
			.expressionAttributeValues(Map.of(
				":pk", SyncRepository.stringValue(SyncRepository.userPk(userId)),
				":postPrefix", SyncRepository.stringValue("POST#")
			))
			.build()).items()
			.stream()
			.map(this::mapUserPost)
			.sorted(Comparator.comparingLong(UserPostResponse::id))
			.toList();
	}

	private UserListItemResponse mapUserListItem(Map<String, AttributeValue> item) {
		return new UserListItemResponse(
			SyncRepository.number(item, "id"),
			SyncRepository.number(item, "externalId"),
			SyncRepository.string(item, "name"),
			SyncRepository.string(item, "username"),
			SyncRepository.string(item, "email"),
			SyncRepository.string(item, "companyName")
		);
	}

	private UserDetailResponse mapUserDetail(Map<String, AttributeValue> item) {
		return new UserDetailResponse(
			SyncRepository.number(item, "id"),
			SyncRepository.number(item, "externalId"),
			SyncRepository.string(item, "name"),
			SyncRepository.string(item, "username"),
			SyncRepository.string(item, "email"),
			SyncRepository.string(item, "phone"),
			SyncRepository.string(item, "website"),
			new UserDetailResponse.AddressResponse(
				SyncRepository.string(item, "addressStreet"),
				SyncRepository.string(item, "addressSuite"),
				SyncRepository.string(item, "addressCity"),
				SyncRepository.string(item, "addressZipcode"),
				new UserDetailResponse.GeoResponse(
					SyncRepository.string(item, "addressGeoLat"),
					SyncRepository.string(item, "addressGeoLng")
				)
			),
			new UserDetailResponse.CompanyResponse(
				SyncRepository.string(item, "companyName"),
				SyncRepository.string(item, "companyCatchPhrase"),
				SyncRepository.string(item, "companyBs")
			)
		);
	}

	private UserPostResponse mapUserPost(Map<String, AttributeValue> item) {
		return new UserPostResponse(
			SyncRepository.number(item, "id"),
			SyncRepository.number(item, "externalId"),
			SyncRepository.string(item, "title"),
			SyncRepository.string(item, "body")
		);
	}
}
