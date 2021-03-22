package com.crio.jumbotail.assettracking.service;

import com.crio.jumbotail.assettracking.entity.Asset;
import com.crio.jumbotail.assettracking.entity.LocationData;
import com.crio.jumbotail.assettracking.exchanges.response.AssetDataResponse;
import com.crio.jumbotail.assettracking.exchanges.response.AssetHistoryResponse;
import java.util.List;

public interface AssetDataRetrievalService {
	@Deprecated
	List<LocationData> getHistoryForAssetOld(Long assetId);

	AssetHistoryResponse getHistoryForAsset(Long assetId);

	Asset getAssetForId(Long assetId);

	AssetDataResponse getAssetFilteredBy(String type, Long startDateTime, Long endDateTime, int limit);

}
