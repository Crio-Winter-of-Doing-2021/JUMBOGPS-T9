package com.crio.jumbotail.assettracking.service;

import com.crio.jumbotail.assettracking.entity.Asset;
import com.crio.jumbotail.assettracking.entity.LocationData;
import com.crio.jumbotail.assettracking.repositories.AssetRepository;
import com.crio.jumbotail.assettracking.repositories.LocationDataRepository;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class AssetDataRetrievalService {

	@Autowired
	private AssetRepository assetRepository;

	@Autowired
	private LocationDataRepository locationDataRepository;

	public List<Asset> getAssets(String assetType, int limit) {
		List<Asset> assets;

		if (!StringUtils.isEmpty(assetType)) {
			assets = assetRepository.findAllByAssetTypeOrderByLastReportedTimestampDesc(assetType, PageRequest.of(0, limit));
		} else {
			assets = assetRepository.findAllByOrderByLastReportedTimestampDesc(PageRequest.of(0, limit));
		}

		return assets;
	}

	public List<Asset> getAssetsWithinTimeRange(Long startTimeStamp, Long endTimeStamp, int limit) {
		return assetRepository.findAllByLastReportedTimestampBetween(
				LocalDateTime.ofEpochSecond(startTimeStamp, 0, ZoneOffset.UTC),
				LocalDateTime.ofEpochSecond(endTimeStamp, 0, ZoneOffset.UTC),
				PageRequest.of(0, limit));
	}

	public List<LocationData> getHistoryForAsset(Long assetId) {

		LocalDateTime endTime = LocalDateTime.now();
		LocalDateTime startTime = LocalDateTime.now().minus(24, ChronoUnit.HOURS);

		final List<LocationData> last24HourHistory = locationDataRepository.findAllByAsset_IdAndTimestampBetween(assetId, startTime, endTime);

		LOG.info("last24HourHistory [{}]", last24HourHistory);

		return last24HourHistory;
	}

}
