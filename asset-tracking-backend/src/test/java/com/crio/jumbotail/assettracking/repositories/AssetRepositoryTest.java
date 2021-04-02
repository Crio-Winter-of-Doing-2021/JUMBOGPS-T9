package com.crio.jumbotail.assettracking.repositories;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;


import com.crio.jumbotail.assettracking.entity.Asset;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.hamcrest.MatcherAssert;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
class AssetRepositoryTest {

	@Autowired
	private AssetRepository assetRepository;

	// USED TO ADJUST AGAINST TIMESTAMPS FOR DB AND JVM
	private static final ZoneOffset offset = OffsetDateTime.now().getOffset();


	@Test
	void service_loads() {
		assertNotNull(assetRepository);
	}

	@Test
	@Sql(scripts = "classpath:prep_data/assets/one_assets.sql")
	void geofence_is_null_if_no_geofence() {
		final Optional<Geometry> geofenceForAsset = assetRepository.getGeofenceForAsset(3000L);
		assertFalse(geofenceForAsset.isPresent());
	}

	@Test
	@Sql(scripts = "classpath:prep_data/assets/one_assets.sql")
	void route_is_null_if_no_route() {
		final Optional<Geometry> routeForAsset = assetRepository.getRouteForAsset(3000L);
		assertFalse(routeForAsset.isPresent());
	}

	@Test
	@Sql(scripts = "classpath:prep_data/assets/one_assets.sql")
	void data_is_fetched() {
		final List<Asset> assets = assetRepository.findAllByOrderByLastReportedTimestampDesc(Pageable.unpaged());
		assertEquals(1, assets.size());
	}

	@Test
	@Sql(scripts = "classpath:prep_data/assets/one_assets.sql")
	void all_assets_when_pageable_obj_is_null() {
		final List<Asset> assets = assetRepository.findAllByOrderByLastReportedTimestampDesc(null);

		assertEquals(1, assets.size());
	}

	@Test
	@Sql(scripts = "classpath:prep_data/assets/one_assets.sql")
	void data_is_fetched_default_delegates_work() {
		final List<Asset> assets = assetRepository.findAssets(Pageable.unpaged());
		assertEquals(1, assets.size());
	}


	@Test
	@Sql(scripts = "classpath:prep_data/assets/one_assets.sql")
	void no_assets_when_asset_type_null() {
		final List<Asset> assets = assetRepository.findAllByAssetTypeOrderByLastReportedTimestampDesc(null, null);

		assertEquals(0, assets.size());
	}

	@Test
	@Sql(scripts = "classpath:prep_data/assets/one_assets.sql")
	void no_assets_when_asset_type_null_default_delegate() {
		final List<Asset> assets = assetRepository.filterAssetsByType(null, null);

		assertEquals(0, assets.size());
	}

	@Test
	@Sql(scripts = "classpath:prep_data/assets/one_assets.sql")
	void no_exception_when_null_pagable() {
		assertDoesNotThrow(() ->
				assetRepository.findAllByOrderByLastReportedTimestampDesc(null));

	}


	@Test
	@Sql(scripts = "classpath:prep_data/assets/one_assets.sql")
	void no_exception_when_null_asset_type() {
		assertDoesNotThrow(() ->
				assetRepository.findAllByAssetTypeOrderByLastReportedTimestampDesc(null, Pageable.unpaged()));
	}

	@Test
	@Sql(scripts = "classpath:prep_data/assets/one_assets.sql")
	void no_assets_when_start_time_param_is_null() {

		assertThrows(RuntimeException.class,
				() -> assetRepository.findAllByAssetTypeAndLastReportedTimestampBetween(
						"TRUCK",
						null,
						LocalDateTime.MIN,
						Pageable.unpaged())
		);

	}


	@Test
	@Sql(scripts = "classpath:prep_data/assets/one_assets.sql")
	void no_assets_when_end_time_param_is_null() {

		assertThrows(RuntimeException.class,
				() -> assetRepository.findAllByAssetTypeAndLastReportedTimestampBetween(
						"TRUCK",
						LocalDateTime.MIN,
						null,
						Pageable.unpaged())
		);

	}

	@Test
	@Sql(scripts = "classpath:prep_data/assets/assets_with_diff_types.sql")
	void assets_are_filtered_by_type() {
		final List<Asset> assets = assetRepository.filterAssetsByType("TRUCK", null);

		assertEquals(1, assets.size());
		assertEquals(3000L, assets.get(0).getId());
	}

	// ========================= TEST HAPPY PATH =====================

	@Test
	@Sql(scripts = "classpath:prep_data/assets/assets_with_diff_types.sql")
	void assets_are_sorted_by_timestamp() {
		final List<Asset> assets = assetRepository.findAssets(Pageable.unpaged());

		assertEquals(3, assets.size());

		final List<LocalDateTime> actualDates = assets.stream().map(Asset::getLastReportedTimestamp).collect(Collectors.toList());

		MatcherAssert.assertThat(actualDates,
				IsIterableContainingInOrder.contains(assets.stream().map(Asset::getLastReportedTimestamp).sorted(Comparator.reverseOrder()).toArray()));
	}

	@Test
	@Sql(scripts = "classpath:prep_data/assets/assets_with_diff_types.sql")
	void assets_are_filtered_by_type_2() {
		final List<Asset> assets = assetRepository.filterAssetsByType("SALESPERSON", null);

		assertEquals(2, assets.size());
		assertThat(assets.stream().map(Asset::getId).collect(Collectors.toList())).hasSameElementsAs(Arrays.asList(3001L, 3002L));
//		assertThat(assets.stream().map(Asset::getId).collect(Collectors.toList())).containsExactly(3001L, 3002L);
	}


	@Test
	@Sql(scripts = "classpath:prep_data/assets/assets_with_diff_types.sql")
	void assets_filtered_by_type_are_sorted_by_timestamp() {
//		findAllByAssetTypeOrderByLastReportedTimestampDesc
		final List<Asset> assets = assetRepository.filterAssetsByType("SALESPERSON", null);

		assertEquals(2, assets.size());

		final List<LocalDateTime> actualDates = assets.stream().map(Asset::getLastReportedTimestamp).collect(Collectors.toList());

		MatcherAssert.assertThat(actualDates,
				IsIterableContainingInOrder.contains(assets.stream().map(Asset::getLastReportedTimestamp).sorted(Comparator.reverseOrder()).toArray()));
	}


	@Test
	@Sql(scripts = "classpath:prep_data/assets/many_assets.sql")
	void find_assets_between_timestamp() {
		LocalDateTime highestDateOfPrepData = LocalDateTime.of(2021, Month.MARCH, 24, 22, 48, 8);
		final int totalSeconds = offset.getTotalSeconds();
		final LocalDateTime actualDateTime = highestDateOfPrepData.plusSeconds(totalSeconds);


		final List<Asset> assets = assetRepository.filterAssetsByTime(
				actualDateTime.minusHours(1),
				actualDateTime,
				Pageable.unpaged());
		assertEquals(9, assets.size());

	}


	@Test
	@Sql(scripts = "classpath:prep_data/assets/many_assets.sql")
	void find_assets_by_type_between_timestamp() {
		LocalDateTime highestDateOfPrepData = LocalDateTime.of(2021, Month.MARCH, 24, 22, 48, 8);
		final int totalSeconds = offset.getTotalSeconds();
		final LocalDateTime actualDateTime = highestDateOfPrepData.plusSeconds(totalSeconds);


		final List<Asset> assets = assetRepository.filterAssetsByTypeAndTime(
				"TRUCK",
				actualDateTime.minusHours(1),
				actualDateTime,
				Pageable.unpaged());
		assertEquals(6, assets.size());

	}



//	@Test
//	void doAllQueries() {
//		final Pageable unpaged = PageRequest.of(0, 100);
//		assetRepository.findAllByAssetTypeOrderByLastReportedTimestampDesc("", unpaged);
//		assetRepository.findAllByOrderByLastReportedTimestampDesc(unpaged);
//		assetRepository.findAllByLastReportedTimestampBetween(now(), now(), unpaged);
//		assetRepository.findAllByAssetTypeAndLastReportedTimestampBetween("", now(), now(), unpaged);
//	}

}