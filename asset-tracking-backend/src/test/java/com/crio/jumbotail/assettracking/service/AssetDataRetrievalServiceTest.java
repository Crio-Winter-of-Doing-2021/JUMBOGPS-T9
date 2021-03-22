package com.crio.jumbotail.assettracking.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


import com.crio.jumbotail.assettracking.entity.Asset;
import com.crio.jumbotail.assettracking.entity.LocationData;
import com.crio.jumbotail.assettracking.exceptions.AssetNotFoundException;
import com.crio.jumbotail.assettracking.exchanges.response.AssetDataResponse;
import com.crio.jumbotail.assettracking.repositories.AssetRepository;
import com.crio.jumbotail.assettracking.repositories.LocationDataRepository;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.annotation.DirtiesContext;

@ExtendWith(MockitoExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class AssetDataRetrievalServiceTest {

	@Mock
	private AssetRepository assetRepository;

	@Mock
	private LocationDataRepository locationDataRepository;

	@InjectMocks
	// @Spy // to mock internal method calls https://stackoverflow.com/questions/42371869/mockito-internal-method-call
	AssetDataRetrievalService assetDataRetrievalService = new AssetDataRetrievalServiceImpl();

	@BeforeEach
	void setUp() {
	}

	@AfterEach
	void tearDown() {
	}

	@Test
	void getAssetFilteredBy_when_no_filter_defined() {
		// given
		Mockito.when(assetRepository.findAssets(any())).thenReturn(Collections.emptyList());
		// when
		AssetDataResponse assetDataResponse = assetDataRetrievalService.getAssetFilteredBy("", null, null, 1);
		final List<Asset> assets = assetDataResponse.getAssets();
		// then
		verify(assetRepository, times(1)).findAssets(any());
		assertEquals(0, assets.size());
	}

	@Test
	void getAssetFilteredBy_when_type_filter_defined() {
		// given
		Mockito.when(assetRepository.filterAssetsByType(eq("some_type"), any())).thenReturn(Collections.emptyList());
		// when
		AssetDataResponse assetDataResponse = assetDataRetrievalService.getAssetFilteredBy("some_type", null, null, 1);
		final List<Asset> assets = assetDataResponse.getAssets();
		// then
		verify(assetRepository, times(1)).filterAssetsByType(eq("some_type"), any());
		assertEquals(0, assets.size());
	}

	@Test
	void getAssetFilteredBy_when_time_filter_defined() {
		// given
		Mockito.when(assetRepository.filterAssetsByTime(any(), any(), any())).thenReturn(Collections.emptyList());
		// when
		AssetDataResponse assetDataResponse = assetDataRetrievalService.getAssetFilteredBy("", 0L, 1L, 1);
		final List<Asset> assets = assetDataResponse.getAssets();
		// then
		verify(assetRepository, times(1)).filterAssetsByTime(any(), any(), any());
		assertEquals(0, assets.size());
	}

	@Test
	void getAssetFilteredBy_when_type_and_time_filter_defined() {
		// given
		Mockito.when(assetRepository.filterAssetsByTypeAndTime(eq("type"), any(), any(), any())).thenReturn(Collections.emptyList());
		// when
		AssetDataResponse assetDataResponse = assetDataRetrievalService.getAssetFilteredBy("type", 0L, 1L, 1);
		final List<Asset> assets = assetDataResponse.getAssets();
		// then
		verify(assetRepository, times(1)).filterAssetsByTypeAndTime(eq("type"), any(), any(), any());
		assertEquals(0, assets.size());
	}

	@Test
	void expect_exception_start_timestamp_lt_end_timestamp() {
		// when + then
		assertThrows(IllegalArgumentException.class,
				() -> assetDataRetrievalService.getAssetFilteredBy("", 1L, 0L, 1));
		verify(assetRepository, times(0)).filterAssetsByTime(any(), any(), any());
	}

	@Test
	void expect_exception_start_timestamp_lt_end_timestamp_and_valid_type() {
		// when + then
		assertThrows(IllegalArgumentException.class,
				() -> assetDataRetrievalService.getAssetFilteredBy("type", 1L, 0L, 1));
		verify(assetRepository, times(0)).filterAssetsByTypeAndTime(eq("type"), any(), any(), any());
	}


	@Test
	void test_find_asset_for_valid_id() {
		// given
		final long assetId = 1025L;
		Asset returned = mock(Asset.class, Mockito.RETURNS_DEEP_STUBS);
		Mockito.when(assetRepository.findById(assetId)).thenReturn(Optional.of(returned));
		// when
		final Asset assetForId = assetDataRetrievalService.getAssetForId(assetId);
		// then
		verify(assetRepository, times(1)).findById(assetId);
		assertEquals(returned, assetForId);
	}

	@Test
	void test_find_asset_for_invalid_id() {
		// given
		final long assetId = 1025L;
		// when
		Mockito.when(assetRepository.findById(assetId)).thenReturn(Optional.empty());
		// then
		assertThrows(AssetNotFoundException.class,
				() -> assetDataRetrievalService.getAssetForId(assetId));
		verify(assetRepository, times(1)).findById(assetId);

	}


	@Test
	void get_history_for_valid_asset() {
		final long assetId = 1025L;

		Mockito.when(assetRepository.existsById(assetId)).thenReturn(true);
		Mockito
				.when(locationDataRepository.findAllByAsset_IdAndTimestampBetweenOrderByTimestampDesc(
						eq(assetId), any(LocalDateTime.class), any(LocalDateTime.class)
				))
				.thenReturn(Collections.singletonList(mock(LocationData.class)));

		final List<LocationData> historyForAsset = assetDataRetrievalService.getHistoryForAssetOld(assetId);

		verify(locationDataRepository, times(1)).findAllByAsset_IdAndTimestampBetweenOrderByTimestampDesc(eq(assetId), any(LocalDateTime.class), any(LocalDateTime.class));

		assertEquals(1, historyForAsset.size());
	}


	@Test
	void get_history_for_invalid_asset() {
		final long assetId = 1025L;
		Mockito.when(assetRepository.existsById(assetId)).thenReturn(false);

		assertThrows(AssetNotFoundException.class, () -> assetDataRetrievalService.getHistoryForAssetOld(assetId));

		verify(locationDataRepository, times(0)).findAllByAsset_IdAndTimestampBetweenOrderByTimestampDesc(eq(assetId), any(LocalDateTime.class),
				any(LocalDateTime.class));
	}

}