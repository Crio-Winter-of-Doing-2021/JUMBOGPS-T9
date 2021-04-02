package com.crio.jumbotail.assettracking.exchanges.request;

import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Geometry;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AssetCreationRequest {

	private String title;
	private String description;
	@NotNull
	private LocationDataDto location;
	private String assetType;

	private Geometry geofence;
	private Geometry route;

	public AssetCreationRequest(String title, String description, @NotNull LocationDataDto location, String assetType) {
		this.title = title;
		this.description = description;
		this.location = location;
		this.assetType = assetType;
	}

	public AssetCreationRequest(String title, String description, @NotNull LocationDataDto location, String assetType, Geometry geofence, Geometry route) {
		this.title = title;
		this.description = description;
		this.location = location;
		this.assetType = assetType;
		this.geofence = geofence;
		this.route = route;
	}
}
