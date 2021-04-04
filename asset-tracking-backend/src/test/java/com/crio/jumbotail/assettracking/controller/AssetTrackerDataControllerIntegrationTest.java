package com.crio.jumbotail.assettracking.controller;

import static com.crio.jumbotail.assettracking.testutils.TestUtils.asJsonString;
import static com.crio.jumbotail.assettracking.testutils.TestUtils.gf;
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
import com.crio.jumbotail.assettracking.exchanges.request.AssetCreationRequest;
import com.crio.jumbotail.assettracking.exchanges.request.LocationDataDto;
import com.crio.jumbotail.assettracking.exchanges.request.LocationUpdateRequest;
import com.crio.jumbotail.assettracking.exchanges.response.AssetCreatedResponse;
import com.crio.jumbotail.assettracking.exchanges.response.AssetDataResponse;
import com.crio.jumbotail.assettracking.exchanges.response.AssetHistoryResponse;
import com.crio.jumbotail.assettracking.repositories.AssetRepository;
import com.crio.jumbotail.assettracking.repositories.LocationDataRepository;
import com.crio.jumbotail.assettracking.testutils.TestUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WithMockUser(roles = "ADMIN")
@SpringBootTest(properties = "debug=true")
@ActiveProfiles("inmem-cache")
@AutoConfigureMockMvc
class AssetTrackerDataControllerIntegrationTest {
	private static final ZoneOffset offset = OffsetDateTime.now().getOffset();
	public static final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);

	@Autowired
	MockMvc mockMvc;

	@Value("classpath:locations.csv")
	Resource resourceFile;
	@Autowired
	private AssetRepository assetRepository;
	@Autowired
	private LocationDataRepository locationDataRepository;
	@Autowired
	private ObjectMapper objectMapper;



	@AfterEach
	void tearDown() {
		locationDataRepository.deleteAll();
		locationDataRepository.flush();
		assertEquals(0, locationDataRepository.findAll().size());

		assetRepository.deleteAll();
		assetRepository.flush();
		assertEquals(0, assetRepository.findAll().size());
	}

	@SneakyThrows
	@Test
	void access_to_api_with_auth() {
		mockMvc.perform(MockMvcRequestBuilders.get("/assets"))
				.andExpect(status().isOk());
	}


	@SneakyThrows
	@Test
	void asset_is_created_and_successfully_fetched() throws Exception {
		final int size = 1;
		createAssets(size);

		getAssets()
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.assets", hasSize(size)));
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
		final int size = 101;
		createAssets(size);

		getAssets()
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.assets", hasSize(100)));
	}

	@Test
	void when_query_with_type_returns_assets_with_only_those_types() throws Exception {
		final int truckAssetSize = 20;
		final String assetType = "TRUCK";
		createAssets(truckAssetSize, assetType);
		createAssets(10, "OTHER");

		mockMvc.perform(get("/assets").param("type", assetType))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.assets", hasSize(truckAssetSize)));
	}
	// test with type filter when type not present


	@Test
	void when_filter_data_for_time_range_then_correct_data_is_returned() throws Exception {
		createAssetsWithinTimeframeAndOutliers(20, 45);

		final long startTime = Instant.now().minus(60, ChronoUnit.SECONDS).getEpochSecond();
		final long endTime = Instant.now().plus((60 * 60 * 21), ChronoUnit.SECONDS).getEpochSecond();

		mockMvc.perform(get("/assets")
				.param("startTimeStamp", String.valueOf(startTime))
				.param("endTimeStamp", String.valueOf(endTime))
		)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.assets", hasSize(20)));
	}


	@Test
	void when_data_for_time_range_created_and_get_assets_with_default_limit() throws Exception {
		createAssetsWithinTimeframeAndOutliers(20, 45);

		getAssets()
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.assets", hasSize(65)));
	}

	@Test
	void date_is_returned_as_string_representation() throws Exception {
		createAssets(1);

		// TODO: update test to match exact LocalDateTime

		getAssets()
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.assets", hasSize(1)))
				.andExpect(jsonPath("$.assets[0].lastReportedTimestamp").isString())
				.andExpect(jsonPath("$.assets[0].lastReportedTimestamp", containsString("T")))
				.andDo(print());
	}


	@Test
	void creat_history_within_24h_time_boundary_is_returned_on_query() throws Exception {

		final List<AssetCreatedResponse> assets = createAssets(1);
		final AssetCreatedResponse assetCreatedResponse = assets.get(0);

		final MvcResult result = getAssetHistory(assetCreatedResponse.getId())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.history", hasSize(1)))
				.andReturn();

		final AssetHistoryResponse assetHistoryResponse = objectMapper.readValue(result.getResponse().getContentAsString(), AssetHistoryResponse.class);
		final LocationData[] locationDataArr = assetHistoryResponse.getHistory().toArray(new LocationData[0]);

		final Point pointUpdated = TestUtils.addMetersToCurrent(locationDataArr[0].getCoordinates(), 5000);

		LocationUpdateRequest minus10000seconds = new LocationUpdateRequest(
				new LocationDataDto(pointUpdated,
						Instant.now().minus(10000, ChronoUnit.SECONDS).getEpochSecond()
				));


		updateAssetHistory(minus10000seconds, assetCreatedResponse.getId()).andExpect(status().isOk());

		LocationUpdateRequest plus1minute = new LocationUpdateRequest();
		plus1minute.setLocation(new LocationDataDto(pointUpdated,
				Instant.now().plus(1, MINUTES).getEpochSecond()
		));

		updateAssetHistory(plus1minute, assetCreatedResponse.getId()).andExpect(status().isOk());

		getAssetHistory(assetCreatedResponse.getId())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.history", hasSize(2)))
				.andDo(print())
				.andReturn();

	}

	@Test
	void update_asset_history_and_correct_data_is_returned_for_24_hour_history() throws Exception {

		Point point = gf.createPoint(new Coordinate(78.01154444, 27.16166111));

		long currentTimeMinus24Hours = Instant.now().minus(24, HOURS)
				.plus(1, MINUTES).getEpochSecond();

		LocationDataDto locationDataDto = new LocationDataDto(point, currentTimeMinus24Hours);

		AssetCreationRequest assetCreationRequest = new AssetCreationRequest("", "",
				locationDataDto, "TRUCK");

		final MvcResult mvcResult = createAsset(assetCreationRequest).andReturn();

		final String responseStr = mvcResult.getResponse().getContentAsString();

		final Long idOfAsset = objectMapper.readValue(responseStr, AssetCreatedResponse.class).getId();

		for (int i = 0; i < 5; i++) {
			currentTimeMinus24Hours = currentTimeMinus24Hours + 1800; // 30 minutes
			point = TestUtils.addMetersToCurrent(point, 5000); // add 5000 meters to last known location

			LocationUpdateRequest updateRequest = new LocationUpdateRequest(
					new LocationDataDto(point, currentTimeMinus24Hours)
			);

			updateAssetHistory(updateRequest, idOfAsset).andExpect(status().isOk());
		}

		final MvcResult result = getAssetHistory(idOfAsset)

				.andExpect(status().isOk())
				.andExpect(jsonPath("$.history", hasSize(6)))
				.andReturn();

	}

	@Test
	void update_asset_history_and_correct_data_is_returned_latest_location() throws Exception {

		Point firstLocation = gf.createPoint(new Coordinate(78.01154444, 27.16166111));


		long firstTimestamp = Instant.now().minus(24, HOURS).plus(1, MINUTES).getEpochSecond();

		LocationDataDto locationDataDto = new LocationDataDto(firstLocation, firstTimestamp);

		AssetCreationRequest assetCreationRequest = new AssetCreationRequest(
				randomAlphabetic(10), randomAlphabetic(40),
				locationDataDto, "TRUCK");

		final MvcResult mvcResult = createAsset(assetCreationRequest).andReturn();

		final String responseStr = mvcResult.getResponse().getContentAsString();

		final Long idOfAsset = objectMapper.readValue(responseStr, AssetCreatedResponse.class).getId();

		long updatedTimestamp = firstTimestamp;
		Point updatedLocation = gf.createPoint(firstLocation.getCoordinate());
		for (int i = 0; i < 5; i++) {
			updatedTimestamp = updatedTimestamp + 1800; // 30 minutes
			updatedLocation = TestUtils.addMetersToCurrent(updatedLocation, 5000); // add 5000 meters to last known location

			LocationUpdateRequest updateRequest = new LocationUpdateRequest();

			updateRequest.setLocation(new LocationDataDto(updatedLocation, updatedTimestamp));

			updateAssetHistory(updateRequest, idOfAsset).andExpect(status().isOk());
		}

		final MvcResult result = getAssets(1).andReturn();
		final AssetDataResponse assetDataResponse = objectMapper.readValue(result.getResponse().getContentAsString(), AssetDataResponse.class);
		final Asset[] assets = assetDataResponse.getAssets().toArray(new Asset[0]);
		assertEquals(1, assets.length);

		assertNotEquals(firstLocation.getX(), assets[0].getLastReportedCoordinates().getX());
		assertNotEquals(firstLocation.getY(), assets[0].getLastReportedCoordinates().getY());
		assertNotEquals(firstTimestamp, assets[0].getLastReportedTimestamp().toInstant(offset).getEpochSecond());

		assertEquals(updatedLocation.getY(), assets[0].getLastReportedCoordinates().getY());
		assertEquals(updatedLocation.getX(), assets[0].getLastReportedCoordinates().getX());
		assertEquals(updatedTimestamp, assets[0].getLastReportedTimestamp().toInstant(offset).getEpochSecond());

	}

	@Test
	void asset_created_then_updated_then_latest_location_is_also_updated() throws Exception {

		Point firstLocation = gf.createPoint(new Coordinate(78.01154444, 27.16166111));

		long currentTimestamp = Instant.now().getEpochSecond();

		LocationDataDto locationDataDto = new LocationDataDto(firstLocation, currentTimestamp);

		AssetCreationRequest assetCreationRequest = new AssetCreationRequest(
				randomAlphabetic(10), randomAlphabetic(40),
				locationDataDto, "TRUCK");

		final MvcResult mvcResult = createAsset(assetCreationRequest).andReturn();

		final String responseStr = mvcResult.getResponse().getContentAsString();

		final Long idOfAsset = objectMapper.readValue(responseStr, AssetCreatedResponse.class).getId();

		LocationUpdateRequest updateRequest = new LocationUpdateRequest();

		final Point updatedLocation = TestUtils.addMetersToCurrent(firstLocation, 5000);
		final long updatedTimestamp = currentTimestamp - 1800;
		updateRequest.setLocation(new LocationDataDto(updatedLocation, updatedTimestamp));

		updateAssetHistory(updateRequest, idOfAsset).andExpect(status().isOk());


		final MvcResult result = getAssets(1).andReturn();
		final AssetDataResponse assetDataResponse = objectMapper.readValue(result.getResponse().getContentAsString(), AssetDataResponse.class);
		final Asset[] assets = assetDataResponse.getAssets().toArray(new Asset[0]);
		assertEquals(1, assets.length);

		assertEquals(updatedLocation.getY(), assets[0].getLastReportedCoordinates().getY());
		assertEquals(updatedLocation.getX(), assets[0].getLastReportedCoordinates().getX());

		assertNotEquals(firstLocation.getY(), assets[0].getLastReportedCoordinates().getY());
		assertNotEquals(firstLocation.getX(), assets[0].getLastReportedCoordinates().getX());


		assertEquals(updatedTimestamp, assets[0].getLastReportedTimestamp().toInstant(offset).getEpochSecond());
		assertNotEquals(currentTimestamp, assets[0].getLastReportedTimestamp().toInstant(offset).getEpochSecond());

	}

	@SneakyThrows
	@Test
	void test_export_data() {
		final MockHttpServletResponse response = mockMvc.perform(get("/assets/export"))
				.andReturn().getResponse();

		assertEquals("text/csv", response.getContentType());
	}

	@SneakyThrows
	@Test
	void test_export_data_with_records() {
		final List<AssetCreatedResponse> assets = createAssets(1);
		final Long id = assets.get(0).getId();
		final MockHttpServletResponse response = mockMvc.perform(get("/assets/export"))
				.andReturn().getResponse();

		final String contentAsString = response.getContentAsString();
		assertTrue(contentAsString.contains(String.valueOf(id)));
	}

	@SneakyThrows
	@Test
	void test_asset_by_id_when_asset_not_present() {
		final long id = 1000L;
		mockMvc.perform(get("/assets/"+ id))
				.andExpect(status().isNotFound());
	}

	@SneakyThrows
	@Test
	void test_asset_by_id_when_asset_present() {
		final List<AssetCreatedResponse> assets = createAssets(1);
		final Long id = assets.get(0).getId();
		mockMvc.perform(get("/assets/"+id))
				.andExpect(status().isOk());
	}

	ResultActions getAssetHistory(long assetId) throws Exception {
		return mockMvc.perform(get("/assets/" + assetId + "/history"));
	}

	ResultActions createAsset(AssetCreationRequest assetCreationRequest) throws Exception {
		return mockMvc.perform(
				post("/api/assets")
						.content(asJsonString(assetCreationRequest))
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON));
	}

	ResultActions updateAssetHistory(LocationUpdateRequest locationUpdateRequest, long assetId) throws Exception {
		return mockMvc.perform(
				patch("/api/assets/" + assetId)
						.content(asJsonString(locationUpdateRequest))
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)

		);

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

			final long epochSecondTimestamp = Instant.now()
					.plus(RandomUtils.nextLong(minSecondsToAdd, maxSecondsToAdd),
							ChronoUnit.SECONDS)
					.getEpochSecond();
			LocationDataDto locationDataDto = new LocationDataDto(
					geometryFactory.createPoint(new Coordinate(Double.parseDouble(s[0]), Double.parseDouble(s[1])))
					, epochSecondTimestamp);

			AssetCreationRequest assetCreationRequest = new AssetCreationRequest(
					randomAlphabetic(10),
					randomAlphabetic(40),
					locationDataDto,
					assetType != null ?
							assetType :
							(RandomUtils.nextInt() % 2 == 0 ? "TRUCK" : "SALESPERSON"));

			final MvcResult mvcResult = mockMvc.perform(
					post("/api/assets")
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