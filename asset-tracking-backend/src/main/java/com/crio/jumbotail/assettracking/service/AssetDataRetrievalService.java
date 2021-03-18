package com.crio.jumbotail.assettracking.service;

import com.crio.jumbotail.assettracking.entity.Asset;
import com.crio.jumbotail.assettracking.entity.LocationData;
import java.util.List;

public interface AssetDataRetrievalService {

	@Deprecated
	List<Asset> getAssets(String assetType, int limit);

	@Deprecated
	List<Asset> getAssetsWithinTimeRange(Long startTimeStamp, Long endTimeStamp, int limit);

	List<LocationData> getHistoryForAsset(Long assetId);

	Asset getAssetForId(Long assetId);

	List<Asset> getAssetFilteredBy(String type, Long startDateTime, Long endDateTime, int limit);

}
