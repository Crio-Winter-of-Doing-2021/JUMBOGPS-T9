package com.crio.jumbotail.assettracking.controller;

import static com.opencsv.ICSVWriter.DEFAULT_QUOTE_CHARACTER;


import com.crio.jumbotail.assettracking.entity.Asset;
import com.crio.jumbotail.assettracking.exchanges.response.AssetDataResponse;
import com.crio.jumbotail.assettracking.exchanges.response.AssetExportData;
import com.crio.jumbotail.assettracking.exchanges.response.AssetHistoryResponse;
import com.crio.jumbotail.assettracking.service.AssetDataRetrievalService;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@Tag(name = "Asset Tracker", description = "The Asset Tracker API")
@SecurityRequirement(name = "bearerAuth")
public class AssetTrackerDataController {

	@Autowired
	private AssetDataRetrievalService retrievalService;

	@Operation(description = "Get last N Assets sorted by timestamp, supports following filter combinations: \n"
	                         + "1. Type\n"
	                         + "2. Type + Start Timestamp & End Timestamp\n"
	                         + "3. Start Timestamp & End Timestamp\n"
	                         + "All combinations support the limit parameter\n",
			summary = "Get Assets and apply filters"
	)
	@ApiResponse(responseCode = "200", description = "Found the assets")
	@GetMapping(value = "/assets")
	public AssetDataResponse getAssetsWithCentroid(
			@Parameter(description = "The max number of results to return") @RequestParam(required = false, defaultValue = "100") int limit,
			@Parameter(description = "The type of to be assets to be filtered") @RequestParam(required = false) String type,
			@Parameter(description = "Starting timestamp in UTC - 0 is epoch time at January 1, 1970 12:00:00 AM", example = "0") @RequestParam(required = false) Long startTimeStamp,
			@Parameter(description = "Ending timestamp in UTC - 1609459200 is epoch time at January 1, 2021 12:00:00 AM", example = "1609459200") @RequestParam(required = false) Long endTimeStamp) {

		AssetDataResponse assetDataResponse = retrievalService.getAssetFilteredBy(type, startTimeStamp, endTimeStamp, limit);

		LOG.info("assets.size() [{}]", assetDataResponse.getAssets().size());

		return assetDataResponse;
	}


	@Operation(summary = "Get 24 Hour History for Asset", description = "Get 24 Hour History for Asset with given id")
	@ApiResponse(responseCode = "404", description = "Asset not found for given id")
	@GetMapping(value = "/assets/{assetId}/history")
	public AssetHistoryResponse getHistoryForAsset(
			@Parameter(description = "The id of asset") @PathVariable Long assetId) {

		final AssetHistoryResponse assetHistoryResponse = retrievalService.getHistoryForAsset(assetId);

		LOG.info("location history size [{}]", assetHistoryResponse.getHistory().size());

		return assetHistoryResponse;
	}


	@Operation(summary = "Get Single Asset", description = "Get Single Asset By Id")
	@ApiResponse(responseCode = "404", description = "Asset not found for given id")
	@GetMapping(value = "/assets/{assetId}")
	public Asset getAsset(
			@Parameter(description = "The id of asset") @PathVariable Long assetId) {

		Asset asset = retrievalService.getAssetForId(assetId);

		LOG.info("Asset found with [{}]", asset.getId());

		return asset;
	}

	@GetMapping("/assets/export")
	public void exportCSV(HttpServletResponse response) throws Exception {

		//set file name and content type
		String filename = "assets.csv";

		response.setContentType("text/csv");
		response.setHeader(HttpHeaders.CONTENT_DISPOSITION,
				"attachment; filename=\"" + filename + "\"");

		//create a csv writer
		StatefulBeanToCsv<AssetExportData> writer = new StatefulBeanToCsvBuilder<AssetExportData>(response.getWriter())
				.withQuotechar(DEFAULT_QUOTE_CHARACTER)
				.withSeparator('|')
				.withOrderedResults(true)
				.build();

		LOG.info("GOING TO EXPORT");

		writer.write(retrievalService.exportData());

		LOG.info("EXPORT DONE, NOW OVER THE WIRE");

	}

}
