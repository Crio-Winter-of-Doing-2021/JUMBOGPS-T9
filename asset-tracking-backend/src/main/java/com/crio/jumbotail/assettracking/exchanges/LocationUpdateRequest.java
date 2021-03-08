package com.crio.jumbotail.assettracking.exchanges;

import lombok.Data;

@Data
public class LocationUpdateRequest {

	Long id;
	LocationDataDto location;

}
