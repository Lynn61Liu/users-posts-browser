package com.example.userspostsbrowser.devtools;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dev")
@ConditionalOnProperty(prefix = "app.dev", name = "reset-enabled", havingValue = "true")
class DevResetController {

	private final DevResetService devResetService;

	DevResetController(DevResetService devResetService) {
		this.devResetService = devResetService;
	}

	@PostMapping("/reset")
	DevResetResult resetDatabase() {
		return devResetService.resetDatabase();
	}
}
