package com.example.userspostsbrowser.devtools;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class DevResetService {

	private final JdbcTemplate jdbcTemplate;

	DevResetService(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Transactional
	DevResetResult resetDatabase() {
		jdbcTemplate.execute("truncate table posts, users, raw_source restart identity cascade");
		return new DevResetResult("success", "Development database reset successfully.");
	}
}
