package com.crio.jumbotail.assettracking.controller;

import static com.crio.jumbotail.assettracking.testutils.TestUtils.asEpoch;
import static com.crio.jumbotail.assettracking.testutils.TestUtils.asJsonString;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
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

		mockMvc.perform(get("/assets"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(size)));
//				.andDo(print());
	}


	//	@Transactional
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

	//	@Transactional
	@Test
	void when_no_query_returns_max_100_assets() throws Exception {
		final int size = 20;
		createAssets(size);

		mockMvc.perform(get("/assets"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(size)));
	}

	//	@Transactional
	@Test
	void when_query_with_type_returns_assets_with_only_those_types() throws Exception {
		createAssets(20, "TRUCK");
		createAssets(10, "OTHER");

		mockMvc.perform(get("/assets?type=TRUCK"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(20)));
	}

	// test with type filter when type not present
//	@Transactional
	@Test
	void when_filter_data_for_time_range_then_correct_data_is_returned() throws Exception {
		createAssetsWithinTimeframeAndOutliers(20, 45);

		final long startTime = LocalDateTime.now().minus(60, ChronoUnit.SECONDS).toEpochSecond(ZoneOffset.UTC);
		final long endTime = LocalDateTime.now().plus((60 * 60 * 21), ChronoUnit.SECONDS).toEpochSecond(ZoneOffset.UTC);

		mockMvc.perform(get("/assets/time")
				.param("startDateTime", String.valueOf(startTime))
				.param("endDateTime", String.valueOf(endTime))
		)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(20)));
	}

	//	@Transactional
	@Test
	void when_data_for_time_range_created_and_get_assets_with_default_limit() throws Exception {
		createAssetsWithinTimeframeAndOutliers(20, 45);

		mockMvc.perform(get("/assets"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(65)));
	}

	/*
	.andExpect(jsonPath("$[*].id", containsInAnyOrder("321", "123")))
	.andExpect(jsonPath("$[*].created", containsInAnyOrder("2019-03-01", "2019-03-02")))
	.andExpect(jsonPath("$[*].updated", containsInAnyOrder("2019-03-15", "2019-03-16")))
	*/
	@Test
//	@Transactional
	void date_is_returned_as_string_representation() throws Exception {
		createAssets(1);

		// TODO: update test to match exact LocalDateTime

		mockMvc.perform(get("/assets"))
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
	void create_asset_at_start_of_day_update_every_30_minutes_view_history() throws Exception {

		LocationDto locationDto = new LocationDto(78.01154444	,27.16166111);

		long epochSecondTimestamp =
				LocalDateTime.of(LocalDate.now(), LocalTime.MIN).toEpochSecond(ZoneOffset.UTC);

		LocationDataDto locationDataDto = new LocationDataDto(
				locationDto,
				epochSecondTimestamp);

		AssetCreationRequest assetCreationRequest = new AssetCreationRequest(
				randomAlphabetic(10), randomAlphabetic(40),
				locationDataDto, "TRUCK" );

		final MvcResult mvcResult = mockMvc.perform(
				post("/assets")
						.content(asJsonString(assetCreationRequest))
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)

		).andReturn();

		final String responseStr = mvcResult.getResponse().getContentAsString();

		final Long idOfAsset = objectMapper.readValue(responseStr, AssetCreatedResponse.class).getId();

		// wrong condition
		// calculate what will be history from current

		for (int i = 0; i < 24 * 2; i++) {
			epochSecondTimestamp = epochSecondTimestamp + 1800; // 30 minutes
			locationDto = utils.addMetersToCurrent(locationDto, 5000); // add 5000 meters to last known location

			LocationUpdateRequest updateRequest = new LocationUpdateRequest();
			updateRequest.setId(idOfAsset);
			updateRequest.setLocation(new LocationDataDto(locationDto, epochSecondTimestamp));

			updateAssetHistory(updateRequest, idOfAsset).andExpect(status().isOk());
		}

		final MvcResult result = getAssetHistory(idOfAsset)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(lessThanOrEqualTo(49))))
				.andReturn();

		final String contentAsString = result.getResponse().getContentAsString();
		final List list = objectMapper.readValue(contentAsString, List.class);
		System.out.println("list size " + list.size());
		System.out.println(list);
		System.out.println("list size " + list.size());

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
					.toEpochSecond(ZoneOffset.UTC);
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
