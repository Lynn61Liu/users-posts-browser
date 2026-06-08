package com.example.userspostsbrowser.importflow;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.userspostsbrowser.importflow.dto.ImportedPostRecord;
import com.example.userspostsbrowser.importflow.dto.ImportedUserRecord;

@Repository
public class SyncRepository {

	private final JdbcTemplate jdbcTemplate;

	public SyncRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public Optional<RawSourceSnapshot> findRawSource(String sourceType, long externalId) {
		return jdbcTemplate.query("""
				select id, payload_hash
				from raw_source
				where source_type = ? and external_id = ?
				""",
			resultSet -> {
				if (!resultSet.next()) {
					return Optional.empty();
				}
				return Optional.of(new RawSourceSnapshot(
					resultSet.getLong("id"),
					resultSet.getString("payload_hash")
				));
			},
			sourceType,
			externalId
		);
	}

	public long insertRawSource(String sourceType, long externalId, String rawPayload, String payloadHash, Instant syncedAt, String syncResult, String syncBatchId) {
		Long id = jdbcTemplate.queryForObject("""
			insert into raw_source (
				source_type,
				external_id,
				raw_payload,
				payload_hash,
				synced_at,
				sync_result,
				sync_batch_id
			)
			values (?, ?, ?::jsonb, ?, ?, ?, ?)
			returning id
			""",
			Long.class,
			sourceType,
			externalId,
			rawPayload,
			payloadHash,
			Timestamp.from(syncedAt),
			syncResult,
			syncBatchId
		);
		return id;
	}

	public void updateRawSource(long id, String rawPayload, String payloadHash, Instant syncedAt, String syncResult, String syncBatchId) {
		jdbcTemplate.update("""
			update raw_source
			set raw_payload = ?::jsonb,
			    payload_hash = ?,
			    synced_at = ?,
			    sync_result = ?,
			    sync_batch_id = ?
			where id = ?
			""",
			rawPayload,
			payloadHash,
			Timestamp.from(syncedAt),
			syncResult,
			syncBatchId,
			id
		);
	}

	public long findUserIdByExternalId(long externalId) {
		Long id = jdbcTemplate.queryForObject("""
			select id
			from users
			where external_id = ?
			""", Long.class, externalId);
		if (id == null) {
			throw new IllegalStateException("User " + externalId + " was not found");
		}
		return id;
	}

	public long upsertUser(ImportedUserRecord user, long rawSourceId, Instant now) {
		Long id = jdbcTemplate.queryForObject("""
			insert into users (
				external_id,
				raw_source_id,
				name,
				username,
				email,
				phone,
				website,
				address_street,
				address_suite,
				address_city,
				address_zipcode,
				address_geo_lat,
				address_geo_lng,
				company_name,
				company_catch_phrase,
				company_bs,
				created_at,
				updated_at
			)
			values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
			on conflict (external_id) do update set
				raw_source_id = excluded.raw_source_id,
				name = excluded.name,
				username = excluded.username,
				email = excluded.email,
				phone = excluded.phone,
				website = excluded.website,
				address_street = excluded.address_street,
				address_suite = excluded.address_suite,
				address_city = excluded.address_city,
				address_zipcode = excluded.address_zipcode,
				address_geo_lat = excluded.address_geo_lat,
				address_geo_lng = excluded.address_geo_lng,
				company_name = excluded.company_name,
				company_catch_phrase = excluded.company_catch_phrase,
				company_bs = excluded.company_bs,
				updated_at = excluded.updated_at
			returning id
			""",
			Long.class,
			user.externalId(),
			rawSourceId,
			user.name(),
			user.username(),
			user.email(),
			user.phone(),
			user.website(),
			user.addressStreet(),
			user.addressSuite(),
			user.addressCity(),
			user.addressZipcode(),
			user.addressGeoLat(),
			user.addressGeoLng(),
			user.companyName(),
			user.companyCatchPhrase(),
			user.companyBs(),
			Timestamp.from(now),
			Timestamp.from(now)
		);
		return id;
	}

	public long upsertPost(ImportedPostRecord post, long rawSourceId, long userId, Instant now) {
		Long id = jdbcTemplate.queryForObject("""
			insert into posts (
				external_id,
				raw_source_id,
				user_id,
				title,
				body,
				created_at,
				updated_at
			)
			values (?, ?, ?, ?, ?, ?, ?)
			on conflict (external_id) do update set
				raw_source_id = excluded.raw_source_id,
				user_id = excluded.user_id,
				title = excluded.title,
				body = excluded.body,
				updated_at = excluded.updated_at
			returning id
			""",
			Long.class,
			post.externalId(),
			rawSourceId,
			userId,
			post.title(),
			post.body(),
			Timestamp.from(now),
			Timestamp.from(now)
		);
		return id;
	}

}

record RawSourceSnapshot(long id, String payloadHash) {
}
