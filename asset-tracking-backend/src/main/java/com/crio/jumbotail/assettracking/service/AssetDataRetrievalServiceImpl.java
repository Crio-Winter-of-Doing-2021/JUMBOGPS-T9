package com.crio.jumbotail.assettracking.service;

import static java.time.ZoneId.systemDefault;
import static java.time.temporal.ChronoUnit.HOURS;


import com.crio.jumbotail.assettracking.entity.Asset;
import com.crio.jumbotail.assettracking.entity.LocationData;
import com.crio.jumbotail.assettracking.repositories.AssetRepository;
import com.crio.jumbotail.assettracking.repositories.LocationDataRepository;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class AssetDataRetrievalServiceImpl implements AssetDataRetrievalService {

	@Autowired
	private AssetRepository assetRepository;

	@Autowired
	private LocationDataRepository locationDataRepository;

	@Override
	public List<Asset> getAssets(String assetType, int limit) {
		List<Asset> assets;

		if (!StringUtils.isEmpty(assetType)) {
			assets = assetRepository.findAllByAssetTypeOrderByLastReportedTimestampDesc(assetType, PageRequest.of(0, limit));
		} else {
			assets = assetRepository.findAllByOrderByLastReportedTimestampDesc(PageRequest.of(0, limit));
		}

		return assets;
	}

	@Override
	public List<Asset> getAssetsWithinTimeRange(Long startTimeStamp, Long endTimeStamp, int limit) {

		final LocalDateTime startTimestamp = LocalDateTime.ofInstant(Instant.ofEpochSecond(startTimeStamp), systemDefault());
		final LocalDateTime endTimestamp = LocalDateTime.ofInstant(Instant.ofEpochSecond(endTimeStamp), systemDefault());

		return assetRepository.findAllByLastReportedTimestampBetween(
				startTimestamp, endTimestamp,
				PageRequest.of(0, limit));
	}

	@Override
	public List<LocationData> getHistoryForAsset(Long assetId) {

		final Instant instant = Instant.now();
		final LocalDateTime currentTimestamp = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
		final LocalDateTime twentyFourHourBeforeTimestamp = LocalDateTime.ofInstant(instant, ZoneOffset.UTC).minus(24, HOURS);


		LOG.info("Checking for data between {} and {}", twentyFourHourBeforeTimestamp, currentTimestamp);

		final List<LocationData> last24HourHistory = locationDataRepository.findAllByAsset_IdAndTimestampBetweenOrderByTimestampDesc(assetId,
				LocalDateTime.now().minus(24, HOURS),
				LocalDateTime.now());

		LOG.info("last24HourHistory [{}]", last24HourHistory);

		return last24HourHistory;
	}

}
