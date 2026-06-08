package com.example.userspostsbrowser.importflow;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
class ImportFlowConfiguration {

	@Bean
	ObjectMapper objectMapper() {
		return new ObjectMapper();
	}
}
