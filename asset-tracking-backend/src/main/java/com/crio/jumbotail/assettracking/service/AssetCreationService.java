package com.crio.jumbotail.assettracking.service;

import com.crio.jumbotail.assettracking.exchanges.AssetCreatedResponse;
import com.crio.jumbotail.assettracking.exchanges.AssetCreationRequest;
import com.crio.jumbotail.assettracking.exchanges.LocationUpdateRequest;

public interface AssetCreationService {

	AssetCreatedResponse createAsset(AssetCreationRequest assetCreationRequest);

	void updateLocationDataForAsset(LocationUpdateRequest locationUpdateRequest, Long assetId);

	void addBoundaryToAsset(Long assetId, String boundaryType, String data);

}
