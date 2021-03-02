package com.crio.jumbotail.assettracking.exchanges;

import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AssetCreationRequest {

	private String title;
	private String description;
	@NotNull
	private Location location;
	private String assetType;

}
