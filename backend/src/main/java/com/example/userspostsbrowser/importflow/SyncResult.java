package com.example.userspostsbrowser.importflow;

public record SyncResult(
	String status,
	String message,
	int usersProcessed,
	int postsProcessed,
	int rawRecordsProcessed
) {
	public static SyncResult success(String message, int usersProcessed, int postsProcessed, int rawRecordsProcessed) {
		return new SyncResult("success", message, usersProcessed, postsProcessed, rawRecordsProcessed);
	}

	public static SyncResult noChange(String message, int usersProcessed, int postsProcessed, int rawRecordsProcessed) {
		return new SyncResult("no_change", message, usersProcessed, postsProcessed, rawRecordsProcessed);
	}

	public static SyncResult update(String message, int usersProcessed, int postsProcessed, int rawRecordsProcessed) {
		return new SyncResult("update", message, usersProcessed, postsProcessed, rawRecordsProcessed);
	}

	public static SyncResult error(String message) {
		return new SyncResult("error", message, 0, 0, 0);
	}
}
