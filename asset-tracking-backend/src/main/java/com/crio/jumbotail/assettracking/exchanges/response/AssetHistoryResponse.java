package com.crio.jumbotail.assettracking.exchanges.response;

import com.crio.jumbotail.assettracking.entity.Asset;
import com.crio.jumbotail.assettracking.entity.LocationData;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.locationtech.jts.geom.Point;

@ToString
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response with the Asset, its location history and centroid")
public class AssetHistoryResponse {

	@Schema(description = "The centroid of the locations for better rendering on map apis")
	private Point centroid;

	@Schema(description = "Asset for the Id found")
	private Asset asset;

	@Schema(description = "The location history for the asset")
	private List<LocationData> history;

}
