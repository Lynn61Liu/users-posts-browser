package com.example.userspostsbrowser.importflow;

import org.springframework.stereotype.Component;

import com.example.userspostsbrowser.importflow.dto.ImportedUserRecord;
import com.example.userspostsbrowser.importflow.dto.JsonPlaceholderUserDto;

@Component
public class UserMapper {

	public ImportedUserRecord toImportedUserRecord(JsonPlaceholderUserDto dto) {
		return new ImportedUserRecord(
			dto.id(),
			dto.name(),
			dto.username(),
			dto.email(),
			dto.phone(),
			dto.website(),
			dto.address() != null ? dto.address().street() : null,
			dto.address() != null ? dto.address().suite() : null,
			dto.address() != null ? dto.address().city() : null,
			dto.address() != null ? dto.address().zipcode() : null,
			dto.address() != null && dto.address().geo() != null ? dto.address().geo().lat() : null,
			dto.address() != null && dto.address().geo() != null ? dto.address().geo().lng() : null,
			dto.company() != null ? dto.company().name() : null,
			dto.company() != null ? dto.company().catchPhrase() : null,
			dto.company() != null ? dto.company().bs() : null
		);
	}
}
