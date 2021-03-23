package com.crio.jumbotail.assettracking.service;

import com.crio.jumbotail.assettracking.exchanges.request.AssetCreationRequest;
import com.crio.jumbotail.assettracking.exchanges.request.LocationUpdateRequest;
import com.crio.jumbotail.assettracking.exchanges.response.AssetCreatedResponse;

public interface AssetCreationService {

	/**
	 *
	 * @param assetCreationRequest request to create a new asset
	 * @return the id of the created asset
	 */
	AssetCreatedResponse createAsset(AssetCreationRequest assetCreationRequest);

	/**
	 *
	 * @param locationUpdateRequest to update the assets loaction
	 * @param assetId the asset id to be updated
	 */
	void updateLocationDataForAsset(LocationUpdateRequest locationUpdateRequest, Long assetId);

	/**
	 *
	 * @param assetId the id of asset to add boundary to
	 * @param boundaryType POLYGON or LINESTRING
	 * @param data the coordinate data fot boundary
	 */
	void addBoundaryToAsset(Long assetId, String boundaryType, String data);

}
