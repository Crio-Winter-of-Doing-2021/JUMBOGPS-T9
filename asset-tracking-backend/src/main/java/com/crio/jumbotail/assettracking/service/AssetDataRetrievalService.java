package com.crio.jumbotail.assettracking.service;

import com.crio.jumbotail.assettracking.entity.Asset;
import com.crio.jumbotail.assettracking.exchanges.response.AssetDataResponse;
import com.crio.jumbotail.assettracking.exchanges.response.AssetHistoryResponse;

public interface AssetDataRetrievalService {

	/**
	 * Get History for the asset
	 * @param assetId the asset id for which history is to be fetched
	 * @return The Asset, its location history and centroid
	 */
	AssetHistoryResponse getHistoryForAsset(Long assetId);

	/**
	 * Finds the asset
	 * @param assetId the asset id to be fetched
	 * @return The Asset for given id
	 */
	Asset getAssetForId(Long assetId);

	/**
	 * Supports multiple operations <br />
	 * No Filtering <br />
	 * Filter asset by type only <br />
	 * Filter asset by time only <br />
	 * Filter asset by type and time <br />
	 * @param type [optional] the type of the asset
	 * @param startDateTime [optional] the start timestamp
	 * @param endDateTime [optional] the end timestamp
	 * @param limit the maximum number of assets to fetch
	 * @return The assets and centroid
	 */
	AssetDataResponse getAssetFilteredBy(String type, Long startDateTime, Long endDateTime, int limit);

}
