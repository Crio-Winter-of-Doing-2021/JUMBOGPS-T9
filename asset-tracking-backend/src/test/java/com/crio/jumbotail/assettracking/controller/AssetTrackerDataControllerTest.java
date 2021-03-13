package com.crio.jumbotail.assettracking.controller;

import static com.crio.jumbotail.assettracking.testutils.TestUtils.asEpoch;
import static com.crio.jumbotail.assettracking.testutils.TestUtils.asJsonString;
import static java.time.ZoneOffset.UTC;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import com.crio.jumbotail.assettracking.entity.Asset;
import com.crio.jumbotail.assettracking.entity.LocationData;
import com.crio.jumbotail.assettracking.exchanges.AssetCreatedResponse;
import com.crio.jumbotail.assettracking.exchanges.AssetCreationRequest;
import com.crio.jumbotail.assettracking.exchanges.LocationDataDto;
import com.crio.jumbotail.assettracking.exchanges.LocationDto;
import com.crio.jumbotail.assettracking.exchanges.LocationUpdateRequest;
import com.crio.jumbotail.assettracking.repositories.AssetRepository;
import com.crio.jumbotail.assettracking.repositories.LocationDataRepository;
import com.crio.jumbotail.assettracking.testutils.TestUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@AutoConfigureMockMvc
@SpringBootTest
class AssetTrackerDataControllerTest {

	@Value("classpath:locations.csv")
	Resource resourceFile;
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private AssetRepository assetRepository;
	@Autowired
	private LocationDataRepository locationDataRepository;
	@Autowired
	private ObjectMapper objectMapper;

	TestUtils utils = new TestUtils();

	@Test
	void asset_is_created_and_successfully_fetched() throws Exception {
		final int size = 1;
		createAssets(size);

		getAssets()
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(size)));
	}


	@Test
	void data_is_created() {
		try {
			mockMvc.perform(get("/create"));
		} catch (Exception exception) {
			exception.printStackTrace();
			fail();
		}
	}

	@Test
	void data_is_created_and_id_is_returned() {
		final List<AssetCreatedResponse> assets = createAssets(1);
		assertEquals(1, assets.size());
		assertNotNull(assets);
		assertNotNull(assets.get(0));
		assertNotNull(assets.get(0).getId());
		assertTrue(assets.get(0).getId() >= 1000);
	}

	@Test
	void when_no_query_returns_max_100_assets() throws Exception {
		final int size = 20;
		createAssets(size);

		getAssets()
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(size)));
	}

	@Test
	void when_query_with_type_returns_assets_with_only_those_types() throws Exception {
		final int truckAssetSize = 20;
		final String assetType = "TRUCK";
		createAssets(truckAssetSize, assetType);
		createAssets(10, "OTHER");

		mockMvc.perform(get("/assets").param("type", assetType))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(truckAssetSize)));
	}

	// test with type filter when type not present
	@Test
	void when_filter_data_for_time_range_then_correct_data_is_returned() throws Exception {
		createAssetsWithinTimeframeAndOutliers(20, 45);

		final long startTime = LocalDateTime.now().minus(60, ChronoUnit.SECONDS).toEpochSecond(UTC);
		final long endTime = LocalDateTime.now().plus((60 * 60 * 21), ChronoUnit.SECONDS).toEpochSecond(UTC);

		mockMvc.perform(get("/assets/time")
				.param("startDateTime", String.valueOf(startTime))
				.param("endDateTime", String.valueOf(endTime))
		)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(20)));
	}

	@Test
	void when_data_for_time_range_created_and_get_assets_with_default_limit() throws Exception {
		createAssetsWithinTimeframeAndOutliers(20, 45);

		getAssets()
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(65)));
	}

	/*
	.andExpect(jsonPath("$[*].id", containsInAnyOrder("321", "123")))
	.andExpect(jsonPath("$[*].created", containsInAnyOrder("2019-03-01", "2019-03-02")))
	.andExpect(jsonPath("$[*].updated", containsInAnyOrder("2019-03-15", "2019-03-16")))
	*/
	@Test
	void date_is_returned_as_string_representation() throws Exception {
		createAssets(1);

		// TODO: update test to match exact LocalDateTime

		getAssets()
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].lastReportedTimestamp").isString())
				.andExpect(jsonPath("$[0].lastReportedTimestamp", containsString("T")))
				.andDo(print());
	}

	@Test
	void creat_history_within_24h_time_boundary_is_returned_on_query() throws Exception {

		final List<AssetCreatedResponse> assets = createAssets(1);
		final AssetCreatedResponse assetCreatedResponse = assets.get(0);

		final MvcResult result = getAssetHistory(assetCreatedResponse.getId())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(1)))
				.andReturn();

		final LocationData[] locationDataArr = objectMapper.readValue(result.getResponse().getContentAsString(), LocationData[].class);
		final LocationDto locationDtoUpdated = utils.addMetersToCurrent(locationDataArr[0].getLocation(), 5000);

		LocationUpdateRequest minus10000seconds = new LocationUpdateRequest(
				assetCreatedResponse.getId(),
				new LocationDataDto(locationDtoUpdated,
						asEpoch(LocalDateTime.now().minus(10000, ChronoUnit.SECONDS))
//						Instant.now().minus(1, ChronoUnit.MINUTES).getEpochSecond()
				));


		updateAssetHistory(minus10000seconds, assetCreatedResponse.getId()).andExpect(status().isOk());

		LocationUpdateRequest plus1minute = new LocationUpdateRequest();
		plus1minute.setId(assetCreatedResponse.getId());
		plus1minute.setLocation(new LocationDataDto(locationDtoUpdated,
				asEpoch(LocalDateTime.now().plus(1, ChronoUnit.MINUTES))
//				Instant.now().plus(1, ChronoUnit.MINUTES).atZone(ZoneOffset.UTC).toEpochSecond()
		));

		updateAssetHistory(plus1minute, assetCreatedResponse.getId()).andExpect(status().isOk());

		getAssetHistory(assetCreatedResponse.getId())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andDo(print())
				.andReturn();

	}

	@Test
	void update_asset_history_and_correct_data_is_returned_for_24_hour_history() throws Exception {

		LocationDto locationDto = new LocationDto(78.01154444, 27.16166111);

		long currentTimeMinus24Hours = LocalDateTime.now().minus(24, HOURS).plus(1, MINUTES).toEpochSecond(UTC);

		long currentTime = LocalDateTime.now().toEpochSecond(UTC);

		LocationDataDto locationDataDto = new LocationDataDto(locationDto, currentTimeMinus24Hours);

		AssetCreationRequest assetCreationRequest = new AssetCreationRequest(
				randomAlphabetic(10), randomAlphabetic(40),
				locationDataDto, "TRUCK");

		final MvcResult mvcResult = mockMvc.perform(
				post("/assets")
						.content(asJsonString(assetCreationRequest))
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)

		).andReturn();

		final String responseStr = mvcResult.getResponse().getContentAsString();

		final Long idOfAsset = objectMapper.readValue(responseStr, AssetCreatedResponse.class).getId();

		for (int i = 0; i < 5; i++) {
			currentTimeMinus24Hours = currentTimeMinus24Hours + 1800; // 30 minutes
			locationDto = utils.addMetersToCurrent(locationDto, 5000); // add 5000 meters to last known location

			LocationUpdateRequest updateRequest = new LocationUpdateRequest();
			updateRequest.setId(idOfAsset);
			updateRequest.setLocation(new LocationDataDto(locationDto, currentTimeMinus24Hours));

			updateAssetHistory(updateRequest, idOfAsset).andExpect(status().isOk());
		}

		final MvcResult result = getAssetHistory(idOfAsset)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(6)))
				.andReturn();

/*
		final String contentAsString = result.getResponse().getContentAsString();
		final List list = objectMapper.readValue(contentAsString, List.class);
		System.out.println("list size " + list.size());
		System.out.println(list);
		System.out.println("list size " + list.size());
		System.out.println("count = " + count);

		final List<LocationData> all = locationDataRepository.findAll();
		System.out.println("all.size() = " + all.size());
//		System.out.println("all = " + all);
*/

	}

	@Test
	void update_asset_history_and_correct_data_is_returned_latest_location() throws Exception {

		LocationDto firstLocation = new LocationDto(78.01154444, 27.16166111);

		long firstTimestamp = LocalDateTime.now().minus(24, HOURS).plus(1, MINUTES).toEpochSecond(UTC);

		long currentTime = LocalDateTime.now().toEpochSecond(UTC);

		LocationDataDto locationDataDto = new LocationDataDto(firstLocation, firstTimestamp);

		AssetCreationRequest assetCreationRequest = new AssetCreationRequest(
				randomAlphabetic(10), randomAlphabetic(40),
				locationDataDto, "TRUCK");

		final MvcResult mvcResult = mockMvc.perform(
				post("/assets")
						.content(asJsonString(assetCreationRequest))
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)

		).andReturn();

		final String responseStr = mvcResult.getResponse().getContentAsString();

		final Long idOfAsset = objectMapper.readValue(responseStr, AssetCreatedResponse.class).getId();

		long updatedTimestamp = firstTimestamp;
		LocationDto updatedLocation = new LocationDto(firstLocation.getLongitude(), firstLocation.getLatitude());
		for (int i = 0; i < 5; i++) {
			updatedTimestamp = updatedTimestamp + 1800; // 30 minutes
			updatedLocation = utils.addMetersToCurrent(updatedLocation, 5000); // add 5000 meters to last known location

			LocationUpdateRequest updateRequest = new LocationUpdateRequest();
			updateRequest.setId(idOfAsset);
			updateRequest.setLocation(new LocationDataDto(updatedLocation, updatedTimestamp));

			updateAssetHistory(updateRequest, idOfAsset).andExpect(status().isOk());
		}

		final MvcResult result = getAssets(1).andReturn();
		final Asset[] assets = objectMapper.readValue(result.getResponse().getContentAsString(), Asset[].class);
		assertEquals(1, assets.length);

		assertNotEquals(firstLocation.getLatitude(), assets[0].getLastReportedLocation().getLatitude());
		assertNotEquals(firstLocation.getLongitude(), assets[0].getLastReportedLocation().getLongitude());
		assertNotEquals(firstTimestamp, assets[0].getLastReportedTimestamp().toEpochSecond(UTC));

		assertEquals(updatedLocation.getLatitude(), assets[0].getLastReportedLocation().getLatitude());
		assertEquals(updatedLocation.getLongitude(), assets[0].getLastReportedLocation().getLongitude());
		assertEquals(updatedTimestamp, assets[0].getLastReportedTimestamp().toEpochSecond(UTC));

	}


	@Test
	void asset_created_then_updated_then_latest_location_is_also_updated() throws Exception {

		LocationDto firstLocation = new LocationDto(78.01154444, 27.16166111);

		long currentTimestamp = LocalDateTime.now().toEpochSecond(UTC);

		LocationDataDto locationDataDto = new LocationDataDto(firstLocation, currentTimestamp);

		AssetCreationRequest assetCreationRequest = new AssetCreationRequest(
				randomAlphabetic(10), randomAlphabetic(40),
				locationDataDto, "TRUCK");

		final MvcResult mvcResult = createAsset(assetCreationRequest).andReturn();

		final String responseStr = mvcResult.getResponse().getContentAsString();

		final Long idOfAsset = objectMapper.readValue(responseStr, AssetCreatedResponse.class).getId();

		LocationUpdateRequest updateRequest = new LocationUpdateRequest();
		updateRequest.setId(idOfAsset);
		final LocationDto updatedLocation = utils.addMetersToCurrent(firstLocation, 5000);
		final long updatedTimestamp = currentTimestamp - 1800;
		updateRequest.setLocation(new LocationDataDto(updatedLocation, updatedTimestamp));

		updateAssetHistory(updateRequest, idOfAsset).andExpect(status().isOk());


		final MvcResult result = getAssets(1).andReturn();
		final Asset[] assets = objectMapper.readValue(result.getResponse().getContentAsString(), Asset[].class);
		assertEquals(1, assets.length);

		assertEquals(updatedLocation.getLatitude(), assets[0].getLastReportedLocation().getLatitude());
		assertEquals(updatedLocation.getLongitude(), assets[0].getLastReportedLocation().getLongitude());

		assertNotEquals(firstLocation.getLatitude(), assets[0].getLastReportedLocation().getLatitude());
		assertNotEquals(firstLocation.getLongitude(), assets[0].getLastReportedLocation().getLongitude());

		assertEquals(updatedTimestamp, assets[0].getLastReportedTimestamp().toEpochSecond(UTC));
		assertNotEquals(currentTimestamp, assets[0].getLastReportedTimestamp().toEpochSecond(UTC));

	}

	ResultActions getAssets() throws Exception {
		return getAssets(-1); // defaults to the limit 100
	}

	ResultActions getAssets(int limit) throws Exception {
		final MockHttpServletRequestBuilder getAssetRequest = get("/assets");
		if (limit > 0) {
			getAssetRequest.param("limit", String.valueOf(limit));
		}
		return mockMvc.perform(getAssetRequest);
	}

	ResultActions createAsset(AssetCreationRequest assetCreationRequest) throws Exception {
		return mockMvc.perform(
				post("/assets")
						.content(asJsonString(assetCreationRequest))
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON));
	}

	ResultActions updateAssetHistory(LocationUpdateRequest locationUpdateRequest, long assetId) throws Exception {
		return mockMvc.perform(
				patch("/assets/" + assetId)
						.content(asJsonString(locationUpdateRequest))
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)

		);

	}

	ResultActions getAssetHistory(long assetId) throws Exception {
		return mockMvc.perform(get("/assets/" + assetId));
	}

	@AfterEach
	void tearDown() {
		locationDataRepository.deleteAll();
		locationDataRepository.flush();
		assertEquals(0, locationDataRepository.findAll().size());

		assetRepository.deleteAll();
		assetRepository.flush();
		assertEquals(0, assetRepository.findAll().size());
	}

	public List<AssetCreatedResponse> createAssets(int number) {
		return createAssets(number, null);
	}

	public List<AssetCreatedResponse> createAssets(int number, String assetType) {
		List<AssetCreatedResponse> assetCreatedResponses = new ArrayList<>();
		try {
			if (number > 200) {
				fail();
			}

			final File file = resourceFile.getFile();
			String data = FileUtils.readFileToString(file, "UTF-8");
			List<String> locations = Arrays.asList(data.split("\n")).subList(0, number);

			assetCreatedResponses = createAssetsForLocations(locations, assetType);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		return assetCreatedResponses;
	}

	public List<AssetCreatedResponse> createAssetsWithinTimeframeAndOutliers(int numberOfAssetWithinTimeframe, int numberOfOutliers) {

		List<AssetCreatedResponse> assetCreatedResponses = new ArrayList<>();
		try {
			if (numberOfAssetWithinTimeframe + numberOfOutliers > 200) {
				fail();
			}

			final File file = resourceFile.getFile();
			String data = FileUtils.readFileToString(file, "UTF-8");
			List<String> locations = Arrays.asList(data.split("\n"));

//			final Path path = Paths.get("C:\\Projects\\Personal\\JUMBOGPS-T9\\asset-tracking-backend\\src\\main\\resources\\locations.csv");
			List<String> locationsForAssetsInTimeframe = locations.subList(0, numberOfAssetWithinTimeframe);
			List<String> locationsForOutliersInTimeframe = locations.subList(numberOfAssetWithinTimeframe, numberOfAssetWithinTimeframe + numberOfOutliers);

			final List<AssetCreatedResponse> inTimeFrame =
					createAssetsForLocations(locationsForAssetsInTimeframe, (60 * 30)/* 30 mins*/, (60 * 60 * 20) /* 20 hrs*/);
			final List<AssetCreatedResponse> outliers =
					createAssetsForLocations(locationsForOutliersInTimeframe, (60 * 60 * 24) /* 20 hrs*/, (60 * 60 * 48) /* 20 hrs*/);

			assetCreatedResponses.addAll(inTimeFrame);
			assetCreatedResponses.addAll(outliers);

		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}

		return assetCreatedResponses;
	}

	public List<AssetCreatedResponse> createAssetsForLocations(List<String> locations) throws Exception {
		return createAssetsForLocations(locations, null);
	}

	public List<AssetCreatedResponse> createAssetsForLocations(List<String> locations, String assetType) throws Exception {
		return createAssetsForLocations(locations, 0, 0, assetType);
	}

	public List<AssetCreatedResponse> createAssetsForLocations(List<String> locations, long minSecondsToAdd, long maxSecondsToAdd) throws Exception {
		return createAssetsForLocations(locations, minSecondsToAdd, maxSecondsToAdd, null);
	}

	public List<AssetCreatedResponse> createAssetsForLocations(List<String> locations, long minSecondsToAdd, long maxSecondsToAdd, String assetType) throws Exception {

		List<AssetCreatedResponse> assetCreatedResponses = new ArrayList<>();

		for (String location : locations) {
			final String[] s = location.split("\t");
			assert (s.length == 2);
			LocationDto locationDto = new LocationDto(Double.valueOf(s[0]), Double.valueOf(s[1]));


			final long epochSecondTimestamp = LocalDateTime.now()
					.plus(RandomUtils.nextLong(minSecondsToAdd, maxSecondsToAdd),
							ChronoUnit.SECONDS)
					.toEpochSecond(UTC);
			LocationDataDto locationDataDto = new LocationDataDto(locationDto, epochSecondTimestamp);

			AssetCreationRequest assetCreationRequest = new AssetCreationRequest(
					randomAlphabetic(10),
					randomAlphabetic(40),
					locationDataDto,
					assetType != null ?
							assetType :
							(RandomUtils.nextInt() % 2 == 0 ? "TRUCK" : "SALESPERSON"));

			final MvcResult mvcResult = mockMvc.perform(
					post("/assets")
							.content(asJsonString(assetCreationRequest))
							.contentType(MediaType.APPLICATION_JSON)
							.accept(MediaType.APPLICATION_JSON)

			).andReturn();

			final String responseStr = mvcResult.getResponse().getContentAsString();

			final AssetCreatedResponse assetCreatedResponse = objectMapper.readValue(responseStr, AssetCreatedResponse.class);

			assetCreatedResponses.add(assetCreatedResponse);
		}

		return assetCreatedResponses;
	}


}
