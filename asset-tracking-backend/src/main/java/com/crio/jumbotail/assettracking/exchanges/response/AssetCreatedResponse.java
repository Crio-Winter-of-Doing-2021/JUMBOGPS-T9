package com.crio.jumbotail.assettracking.exchanges.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Response with id allocated to the posted asset")
public class AssetCreatedResponse {
	/**
	 * The id of the Meme that was stored in the DB
	 */
	@Schema(description = "unique id allocated to the posted asset")
	private Long id;
}
