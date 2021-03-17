package com.crio.jumbotail.assettracking.service;

import static java.time.ZoneId.systemDefault;
import static java.time.temporal.ChronoUnit.HOURS;


import com.crio.jumbotail.assettracking.entity.Asset;
import com.crio.jumbotail.assettracking.entity.LocationData;
import com.crio.jumbotail.assettracking.exceptions.AssetNotFoundException;
import com.crio.jumbotail.assettracking.repositories.AssetRepository;
import com.crio.jumbotail.assettracking.repositories.LocationDataRepository;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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

		final boolean assetExists = assetRepository.existsById(assetId);
		if (!assetExists) {
			throw new AssetNotFoundException("Asset not found for Id - " + assetId);
		}

		final List<LocationData> last24HourHistory = locationDataRepository.findAllByAsset_IdAndTimestampBetweenOrderByTimestampDesc(assetId,
				LocalDateTime.now().minus(24, HOURS),
				LocalDateTime.now());

		LOG.info("last24HourHistory [{}]", last24HourHistory);

		return last24HourHistory;
	}

	@Override
	public Asset getAssetForId(Long assetId) {
		final Optional<Asset> asset = assetRepository.findById(assetId);

		return asset.orElseThrow(() -> new AssetNotFoundException("Asset not found for Id - " + assetId));
	}


	// TODO : filters
	// (all will support limits + and will be sorted based on timestamp)
	// TODO 1. Filter on (start-time and end-time)
	// add validation
	// query - done
	// TODO 2. Filter on (start-time and end-time) + type
	// add validation
	// query - done
	// TODO 3. Filter on type
	// add validation
	// query - done
	@Override
	public List<Asset> getAssetFilteredBy(String assetType, Long startTimestamp, Long endTimestamp, int limit) {
		List<Asset> assets = null;

		final PageRequest pageRequest = PageRequest.of(0, limit);
		if (hasNoFiltersDefined(assetType, startTimestamp, endTimestamp)) {
			LOG.info("No filters defined. Getting all assets.");
			assets = assetRepository.findAssets(pageRequest);
		} else if (hasOnlyTypeFilterDefined(assetType, startTimestamp, endTimestamp)) {
			LOG.info("Type filter defined");
			assets = assetRepository.filterAssetsByType(assetType, pageRequest);
		} else if (hasOnlyTimeFilterDefined(assetType, startTimestamp, endTimestamp)) {
			throwIfInvalidTimeFilter(startTimestamp, endTimestamp);
			final LocalDateTime startDateTime = localDateTimeFromTimestamp(startTimestamp);
			final LocalDateTime endDateTime = localDateTimeFromTimestamp(endTimestamp);
			assets = assetRepository.filterAssetsByTime(startDateTime, endDateTime, pageRequest);
		} else if (hasBothTimeAndTypeFilter(assetType, startTimestamp, endTimestamp)) {
			throwIfInvalidTimeFilter(startTimestamp, endTimestamp);
			final LocalDateTime startDateTime = localDateTimeFromTimestamp(startTimestamp);
			final LocalDateTime endDateTime = localDateTimeFromTimestamp(endTimestamp);
			assets = assetRepository.filterAssetsByTypeAndTime(assetType, startDateTime, endDateTime, pageRequest);
		}

		return assets;
	}

	private void throwIfInvalidTimeFilter(Long startTimestamp, Long endTimestamp) {
		if (!isValidTimeFilter(startTimestamp, endTimestamp)) {
			throw new IllegalArgumentException("Start time should not be less than end date time");
		}
	}

	private boolean hasBothTimeAndTypeFilter(String assetType, Long startTimestamp, Long endTimestamp) {
		return !StringUtils.isEmpty(assetType) && (startTimestamp != null && endTimestamp != null);
	}

	private boolean hasOnlyTimeFilterDefined(String assetType, Long startTimestamp, Long endTimestamp) {
		return StringUtils.isEmpty(assetType) && (startTimestamp != null && endTimestamp != null);
	}

	private boolean hasNoFiltersDefined(String assetType, Long startTimestamp, Long endTimestamp) {
		return StringUtils.isEmpty(assetType) && (startTimestamp == null || endTimestamp == null);
	}

	private boolean hasOnlyTypeFilterDefined(String assetType, Long startTimestamp, Long endTimestamp) {
		return !StringUtils.isEmpty(assetType) && (startTimestamp == null || endTimestamp == null);
	}

	private boolean isValidTimeFilter(Long startTimestamp, Long endTimestamp) {
		// start say 1 - Jan - 2020
		// end say 20 - Jan - 2020
		// is a valid combination
		final boolean isValid = startTimestamp <= endTimestamp;

		LOG.info("startDateTime [{}] , endDateTime [{}], isValid [{}]", startTimestamp, endTimestamp, isValid);

		return isValid;
	}

	private LocalDateTime localDateTimeFromTimestamp(Long timestamp) {
		return LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), systemDefault());
	}

}
