package com.crio.jumbotail.assettracking.repositories;

import com.crio.jumbotail.assettracking.entity.Asset;
import java.time.LocalDateTime;
import java.util.List;
import org.locationtech.jts.geom.Geometry;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {


//	Polygon getAssetById(Long assetId);

	@Query(value = "SELECT asset.geofence from Asset asset where asset.id = ?1")
	Geometry getGeofenceForAsset(Long assetId);

	@Query(value = "SELECT asset.route from Asset asset where asset.id = ?1")
	Geometry getRouteForAsset(Long assetId);

	// last N assets (N = 100 by default)
	List<Asset> findAllByOrderByLastReportedTimestampDesc(
			Pageable pageable
	);


	// asset type filter
	List<Asset> findAllByAssetTypeOrderByLastReportedTimestampDesc(
			String assetType, Pageable pageable
	);


	// time based filter for all assets
	List<Asset> findAllByLastReportedTimestampBetween(
			LocalDateTime startTime, LocalDateTime endTime,
			Pageable pageable
	);

	// time + type filter
	List<Asset> findAllByAssetTypeAndLastReportedTimestampBetween(
			String assetType,
			LocalDateTime startTime, LocalDateTime endTime,
			Pageable pageable
	);

	List<Asset> findAllByAssetTypeInOrderByLastReportedTimestampDesc(
			List<String> assetTypes, Pageable pageable
	);

	List<Asset> findAllByAssetTypeInAndLastReportedTimestampBetween(
			List<String> assetTypes,
			LocalDateTime startTime, LocalDateTime endTime,
			Pageable pageable
	);

	default List<Asset> filterAssetsByType(String assetType, Pageable pageable) {
		return findAllByAssetTypeOrderByLastReportedTimestampDesc(assetType, pageable);
	}

	default List<Asset> filterAssetsByTypeAndTime(String assetType,
	                                              LocalDateTime startTime, LocalDateTime endTime,
	                                              Pageable pageable) {
		return findAllByAssetTypeAndLastReportedTimestampBetween(assetType, startTime, endTime, pageable);
	}

	default List<Asset> findAssets(Pageable pageable) {
		return findAllByOrderByLastReportedTimestampDesc(pageable);
	}


	default List<Asset> filterAssetsByTime(LocalDateTime startDateTime, LocalDateTime endDateTime, PageRequest pageReqWithLimit) {
		return findAllByLastReportedTimestampBetween(startDateTime, endDateTime, pageReqWithLimit);
	}
}
