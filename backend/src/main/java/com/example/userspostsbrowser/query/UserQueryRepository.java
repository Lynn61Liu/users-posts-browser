package com.example.userspostsbrowser.query;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.example.userspostsbrowser.query.dto.UserDetailResponse;
import com.example.userspostsbrowser.query.dto.UserListItemResponse;
import com.example.userspostsbrowser.query.dto.UserPostResponse;

@Repository
class UserQueryRepository {

	private final JdbcTemplate jdbcTemplate;

	UserQueryRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	List<UserListItemResponse> findAllUsers() {
		return jdbcTemplate.query("""
			select id, external_id, name, username, email, company_name
			from users
			order by id
			""",
			(this::mapUserListItem)
		);
	}

	Optional<UserDetailResponse> findUserById(long userId) {
		List<UserDetailResponse> results = jdbcTemplate.query("""
			select id,
			       external_id,
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
			       company_bs
			from users
			where id = ?
			""",
			this::mapUserDetail,
			userId
		);
		return results.stream().findFirst();
	}

	List<UserPostResponse> findPostsByUserId(long userId) {
		return jdbcTemplate.query("""
			select id, external_id, title, body
			from posts
			where user_id = ?
			order by id
			""",
			this::mapUserPost,
			userId
		);
	}

	private UserListItemResponse mapUserListItem(ResultSet resultSet, int rowNum) throws SQLException {
		return new UserListItemResponse(
			resultSet.getLong("id"),
			resultSet.getLong("external_id"),
			resultSet.getString("name"),
			resultSet.getString("username"),
			resultSet.getString("email"),
			resultSet.getString("company_name")
		);
	}

	private UserDetailResponse mapUserDetail(ResultSet resultSet, int rowNum) throws SQLException {
		return new UserDetailResponse(
			resultSet.getLong("id"),
			resultSet.getLong("external_id"),
			resultSet.getString("name"),
			resultSet.getString("username"),
			resultSet.getString("email"),
			resultSet.getString("phone"),
			resultSet.getString("website"),
			new UserDetailResponse.AddressResponse(
				resultSet.getString("address_street"),
				resultSet.getString("address_suite"),
				resultSet.getString("address_city"),
				resultSet.getString("address_zipcode"),
				new UserDetailResponse.GeoResponse(
					resultSet.getString("address_geo_lat"),
					resultSet.getString("address_geo_lng")
				)
			),
			new UserDetailResponse.CompanyResponse(
				resultSet.getString("company_name"),
				resultSet.getString("company_catch_phrase"),
				resultSet.getString("company_bs")
			)
		);
	}

	private UserPostResponse mapUserPost(ResultSet resultSet, int rowNum) throws SQLException {
		return new UserPostResponse(
			resultSet.getLong("id"),
			resultSet.getLong("external_id"),
			resultSet.getString("title"),
			resultSet.getString("body")
		);
	}
}
