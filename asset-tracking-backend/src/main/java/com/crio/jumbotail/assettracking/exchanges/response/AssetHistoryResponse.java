package com.crio.jumbotail.assettracking.exchanges.response;

import com.crio.jumbotail.assettracking.entity.Asset;
import com.crio.jumbotail.assettracking.entity.Location;
import com.crio.jumbotail.assettracking.entity.LocationData;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssetHistoryResponse {

	@Schema(description = "The centroid of the assets")
	private Location centroid;

	@Schema(description = "Asset for the Id found")
	private Asset asset;

	private List<LocationData> history;

}
