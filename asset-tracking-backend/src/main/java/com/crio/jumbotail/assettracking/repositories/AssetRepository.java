package com.crio.jumbotail.assettracking.repositories;

import com.crio.jumbotail.assettracking.entity.Asset;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {

	// last (100 by default) assets
	List<Asset> findAllByOrderByLastReportedTimestampDesc(Pageable pageable);


	// asset type filter
	List<Asset> findAllByAssetTypeOrderByLastReportedTimestampDesc(String assetType, Pageable pageable);


	// time based filter for all assets
	List<Asset> findAllByLastReportedTimestampBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);




}
