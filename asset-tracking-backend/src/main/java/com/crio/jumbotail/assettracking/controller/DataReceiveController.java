package com.crio.jumbotail.assettracking.controller;

import com.crio.jumbotail.assettracking.exchanges.AssetCreatedResponse;
import com.crio.jumbotail.assettracking.exchanges.AssetCreationRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DataReceiveController {

	/**
	 * @param assetCreationRequest request to create a new asset with its initial location
	 * @return id of the created asset
	 */
	@PostMapping("/assets")
	@ResponseStatus(HttpStatus.CREATED)
	public AssetCreatedResponse createNewAsset(AssetCreationRequest assetCreationRequest) {
		return null;
	}

	@PatchMapping("/assets")
	public void updateLocationOfAsset() {

	}

}
