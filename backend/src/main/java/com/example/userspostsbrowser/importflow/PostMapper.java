package com.example.userspostsbrowser.importflow;

import org.springframework.stereotype.Component;

import com.example.userspostsbrowser.importflow.dto.ImportedPostRecord;
import com.example.userspostsbrowser.importflow.dto.JsonPlaceholderPostDto;

@Component
public class PostMapper {

	public ImportedPostRecord toImportedPostRecord(JsonPlaceholderPostDto dto) {
		return new ImportedPostRecord(dto.id(), dto.userId(), dto.title(), dto.body());
	}
}
