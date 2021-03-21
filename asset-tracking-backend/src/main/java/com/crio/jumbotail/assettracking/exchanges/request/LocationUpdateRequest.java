package com.crio.jumbotail.assettracking.exchanges.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor
@AllArgsConstructor
public class LocationUpdateRequest {

	private Long id;
	private LocationDataDto location;

	public LocationUpdateRequest(LocationDataDto location) {
		this.location = location;
	}
}
