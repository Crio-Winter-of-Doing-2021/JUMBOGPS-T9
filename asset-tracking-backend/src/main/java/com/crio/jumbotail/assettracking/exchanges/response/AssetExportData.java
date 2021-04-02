package com.crio.jumbotail.assettracking.exchanges.response;


import com.opencsv.bean.CsvBindByPosition;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.locationtech.jts.geom.Geometry;

/**
 * |Asset Id|Start Timestamp(Epoch)|Start Timestamp(Epoch)|Start Location (Coordinates)| Start Location (Coordinates)|Defined Route|GeoFence|
 */

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssetExportData {

	@CsvBindByPosition(position = 0)
	private Long assetId;
	@CsvBindByPosition(position = 1)
	private String assetType;
	@CsvBindByPosition(position = 2)
	private LocalDateTime startTimestamp;
	@CsvBindByPosition(position = 3)
	private LocalDateTime endTimestamp;
	@CsvBindByPosition(position = 4)
	private Geometry startLocation;
	@CsvBindByPosition(position = 5)
	private Geometry endLocation;
	@CsvBindByPosition(position = 6)
	private Geometry route;
	@CsvBindByPosition(position = 7)
	private Geometry geofence;


}
