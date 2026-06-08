package com.example.userspostsbrowser.importflow;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SyncController {

	private final SyncService syncService;

	public SyncController(SyncService syncService) {
		this.syncService = syncService;
	}

	@PostMapping("/api/sync")
	public SyncResult sync() {
		return syncService.sync();
	}
}
