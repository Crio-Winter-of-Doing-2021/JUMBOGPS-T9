package com.crio.jumbotail.assettracking.repositories;

import com.crio.jumbotail.assettracking.entity.Asset;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.locationtech.jts.geom.Geometry;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {

	@Query(value = "SELECT asset.geofence from Asset asset where asset.id = ?1")
	Optional<Geometry> getGeofenceForAsset(Long assetId);

	@Query(value = "SELECT asset.route from Asset asset where asset.id = ?1")
	Optional<Geometry> getRouteForAsset(Long assetId);

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


//	@Query(value = "SELECT new com.crio.jumbotail.assettracking.exchanges.response.AssetExportData("
//	               + "a.id, a.assetType,a.lastReportedTimestamp, a.lastReportedTimestamp,a.lastReportedCoordinates,a.lastReportedCoordinates,a.route,a.geofence)"
//	               + " FROM Asset a")

//	@Query(value = "SELECT a.id, a.asset_Type, a.last_Reported_Timestamp,"
//	               + "l.timestamp as start_timestamp,l.coordinates as start_location,"
//	               + "a.last_Reported_Coordinates,a.route,a.geofence FROM Asset a, location_data l "
//	               + "where l.id = ("
//	               + "select l.id"
//	               + "from location_data l "
//	               + "where l.asset_id = a.id order by timestamp"
//	               + "desc limit 1"
//	               + ")")
//	@Query(value = "SELECT new com.crio.jumbotail.assettracking.exchanges.response.AssetExportData("
//	               + ")"
//	               + " FROM Asset a, LocationData l WHERE l.id = (SELECT minelement(l.timestamp), l.id FROM LocationData l WHERE l.asset.id = a.id)")
//	List<AssetExportData> exportData();


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


	default List<Asset> filterAssetsByTime(LocalDateTime startDateTime, LocalDateTime endDateTime, Pageable pageable) {
		return findAllByLastReportedTimestampBetween(startDateTime, endDateTime, pageable);
	}
}
