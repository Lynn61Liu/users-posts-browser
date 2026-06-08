package com.example.userspostsbrowser.importflow.dto;

public record ImportedUserRecord(
	long externalId,
	String name,
	String username,
	String email,
	String phone,
	String website,
	String addressStreet,
	String addressSuite,
	String addressCity,
	String addressZipcode,
	String addressGeoLat,
	String addressGeoLng,
	String companyName,
	String companyCatchPhrase,
	String companyBs
) {
}
