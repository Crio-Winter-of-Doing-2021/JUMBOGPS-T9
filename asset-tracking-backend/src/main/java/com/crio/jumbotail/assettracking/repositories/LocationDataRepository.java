package com.crio.jumbotail.assettracking.repositories;

import com.crio.jumbotail.assettracking.entity.LocationData;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationDataRepository extends JpaRepository<LocationData, Long> {

	List<LocationData> findAllByAsset_IdAndTimestampBetweenOrderByTimestampDesc(Long assetId, LocalDateTime twentyFourHourBeforeTimestamp, LocalDateTime currentTime);

//	Page<LocationData> findByAsset_IdOrderByTimestampDesc(Long assetId, Pageable pageable);
//
//	default Page<LocationData> findLastLocationsOfAnAsset(Long assetId) {
//		Pageable singleRecord = PageRequest.of(0, 1);
//		return findByAsset_IdOrderByTimestampDesc(assetId, singleRecord);
//	}

}
