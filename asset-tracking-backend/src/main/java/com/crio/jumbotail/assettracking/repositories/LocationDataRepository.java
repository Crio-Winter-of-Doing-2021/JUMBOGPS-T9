package com.crio.jumbotail.assettracking.repositories;

import com.crio.jumbotail.assettracking.entity.LocationData;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationDataRepository extends JpaRepository<LocationData, Long> {

//	List<LocationData> findAllByAsset_IdAndDeviceTimestampBetween(Long assetId, long startTime, long endTime);
	List<LocationData> findAllByAsset_IdAndTimestampBetween(Long assetId, LocalDateTime startTime, LocalDateTime endTime);


	@Query(value = "SELECT location from LocationData location where location.asset.id = ?1 ORDER BY location.timestamp DESC")
	Page<LocationData> findLastNLocationsOfAnAsset(Long assetId, Pageable pageable);

	Page<LocationData> findByAsset_IdOrderByTimestampDesc(Long assetId, Pageable pageable);


	default Page<LocationData> findLastLocationsOfAnAsset(Long assetId) {
		Pageable singleRecord = PageRequest.of(0, 1);
		return findByAsset_IdOrderByTimestampDesc(assetId,singleRecord);
	}

}
