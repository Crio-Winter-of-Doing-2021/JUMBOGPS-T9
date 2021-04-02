package com.crio.jumbotail.assettracking.exchanges.response;


import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.locationtech.jts.geom.Geometry;

/**
 * |Asset Id|Start Timestamp(Epoch)|Start Timestamp(Epoch)|Start Location (Coordinates)| Start Location (Coordinates)|Defined Route|GeoFence|
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssetExportData {

	private Long assetId;
	private String assetType;
	private LocalDateTime startTimestamp;
	private LocalDateTime endTimestamp;
	private Geometry startLocation;
	private Geometry endLocation;
	private Geometry route;
	private Geometry geofence;


}
