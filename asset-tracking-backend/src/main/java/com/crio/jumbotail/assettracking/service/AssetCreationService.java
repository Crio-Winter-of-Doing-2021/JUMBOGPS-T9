package com.crio.jumbotail.assettracking.service;

import com.crio.jumbotail.assettracking.exchanges.request.AssetCreationRequest;
import com.crio.jumbotail.assettracking.exchanges.request.LocationUpdateRequest;
import com.crio.jumbotail.assettracking.exchanges.response.AssetCreatedResponse;

public interface AssetCreationService {

	AssetCreatedResponse createAsset(AssetCreationRequest assetCreationRequest);

	void updateLocationDataForAsset(LocationUpdateRequest locationUpdateRequest, Long assetId);

	void addBoundaryToAsset(Long assetId, String boundaryType, String data);

}
