package com.crio.jumbotail.assettracking.controller;

import static com.crio.jumbotail.assettracking.testutils.TestUtils.asJsonString;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import com.crio.jumbotail.assettracking.exchanges.AssetCreatedResponse;
import com.crio.jumbotail.assettracking.exchanges.AssetCreationRequest;
import com.crio.jumbotail.assettracking.exchanges.LocationDataDto;
import com.crio.jumbotail.assettracking.exchanges.LocationDto;
import com.crio.jumbotail.assettracking.repositories.AssetRepository;
import com.crio.jumbotail.assettracking.repositories.LocationDataRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@AutoConfigureMockMvc
@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:asset_tracker_test_db"
})
//@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class AssetTrackerDataControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private AssetRepository assetRepository;

	@Autowired
	private LocationDataRepository locationDataRepository;

	@Autowired
	private ObjectMapper objectMapper;

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
		createAssets(20);

		mockMvc.perform(get("/assets"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(20)));
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

	@AfterEach
	void tearDown() {
		locationDataRepository.deleteAll();
		locationDataRepository.flush();
		assertEquals(0, locationDataRepository.findAll().size());

		assetRepository.deleteAll();
		assetRepository.flush();
		assertEquals(0, assetRepository.findAll().size());
	}

	List<AssetCreatedResponse> createAssets(int number) {
		return createAssets(number, null);
	}

	List<AssetCreatedResponse> createAssets(int number, String assetType) {
		List<AssetCreatedResponse> assetCreatedResponses = new ArrayList<>();
		try {
			if (number > 200) {
				fail();
			}
			final Path path = Paths.get("C:\\Projects\\Personal\\JUMBOGPS-T9\\asset-tracking-backend\\src\\main\\resources\\locations.csv");
			List<String> locations = Files.readAllLines(path).subList(0, number);

			assetCreatedResponses = createAssetsForLocations(locations, assetType);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
		return assetCreatedResponses;
	}

	List<AssetCreatedResponse> createAssetsWithinTimeframeAndOutliers(int numberOfAssetWithinTimeframe, int numberOfOutliers) {

		List<AssetCreatedResponse> assetCreatedResponses = new ArrayList<>();
		try {
			if (numberOfAssetWithinTimeframe + numberOfOutliers > 200) {
				fail();
			}
			final Path path = Paths.get("C:\\Projects\\Personal\\JUMBOGPS-T9\\asset-tracking-backend\\src\\main\\resources\\locations.csv");
			List<String> locationsForAssetsInTimeframe = Files.readAllLines(path).subList(0, numberOfAssetWithinTimeframe);
			List<String> locationsForOutliersInTimeframe = Files.readAllLines(path).subList(numberOfAssetWithinTimeframe, numberOfAssetWithinTimeframe + numberOfOutliers);

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

	private List<AssetCreatedResponse> createAssetsForLocations(List<String> locations) throws Exception {
		return createAssetsForLocations(locations, null);
	}

	private List<AssetCreatedResponse> createAssetsForLocations(List<String> locations, String assetType) throws Exception {
		return createAssetsForLocations(locations, 0, 0, assetType);
	}

	private List<AssetCreatedResponse> createAssetsForLocations(List<String> locations, long minSecondsToAdd, long maxSecondsToAdd) throws Exception {
		return createAssetsForLocations(locations, minSecondsToAdd, maxSecondsToAdd, null);
	}

	private List<AssetCreatedResponse> createAssetsForLocations(List<String> locations, long minSecondsToAdd, long maxSecondsToAdd, String assetType) throws Exception {

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
					RandomStringUtils.randomAlphabetic(10),
					RandomStringUtils.randomAlphabetic(40),
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