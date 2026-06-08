package com.example.userspostsbrowser.query.dto;

public record UserDetailResponse(
	long id,
	long externalId,
	String name,
	String username,
	String email,
	String phone,
	String website,
	AddressResponse address,
	CompanyResponse company
) {
	public record AddressResponse(
		String street,
		String suite,
		String city,
		String zipcode,
		GeoResponse geo
	) {
	}

	public record GeoResponse(String lat, String lng) {
	}

	public record CompanyResponse(String name, String catchPhrase, String bs) {
	}
}
