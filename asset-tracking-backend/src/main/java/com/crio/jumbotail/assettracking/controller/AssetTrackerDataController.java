package com.crio.jumbotail.assettracking.controller;

import com.crio.jumbotail.assettracking.entity.Asset;
import com.crio.jumbotail.assettracking.entity.LocationData;
import com.crio.jumbotail.assettracking.service.AssetDataRetrievalService;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
public class AssetTrackerDataController {

	@Autowired
	private AssetDataRetrievalService retrievalService;

	@GetMapping(value = "assets")
	public List<Asset> getAssets(@RequestParam(required = false, defaultValue = "100") int limit,
	                             @RequestParam(required = false) String type) {
		List<Asset> assets = retrievalService.getAssets(type, limit);

		LOG.info("assets.size() [{}]", assets.size());

		return assets;
	}

	@GetMapping(value = "assets/time")
	public List<Asset> getMarkersTimeFilter(@RequestParam Long startDateTime,
	                                        @RequestParam Long endDateTime,
	                                        @RequestParam(required = false, defaultValue = "100") int limit) {

		final List<Asset> assets = retrievalService.getAssetsWithinTimeRange(startDateTime, endDateTime, limit);

		LOG.info("assets.size() [{}]", assets.size());

		return assets;
	}

	@GetMapping(value = "assets/{assetId}")
	public List<LocationData> getHistoryForAsset(@PathVariable Long assetId) {

		final List<LocationData> assetHistory = retrievalService.getHistoryForAsset(assetId);

		LOG.info("location history size [{}]", assetHistory.size());

		return assetHistory;
	}


}
