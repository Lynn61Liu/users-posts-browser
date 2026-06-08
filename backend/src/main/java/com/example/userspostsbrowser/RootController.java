package com.example.userspostsbrowser;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class RootController {

	@GetMapping("/")
	String root() {
		return "Users Posts Browser backend is running. See /actuator/health for the health check.";
	}
}
