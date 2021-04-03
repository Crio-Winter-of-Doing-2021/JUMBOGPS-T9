package com.crio.jumbotail.assettracking.controller;

import static com.crio.jumbotail.assettracking.testutils.TestUtils.asJsonString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import com.crio.jumbotail.assettracking.entity.Asset;
import com.crio.jumbotail.assettracking.entity.LocationData;
import com.crio.jumbotail.assettracking.exchanges.request.AssetCreationRequest;
import com.crio.jumbotail.assettracking.exchanges.request.LocationDataDto;
import com.crio.jumbotail.assettracking.exchanges.request.LocationUpdateRequest;
import com.crio.jumbotail.assettracking.exchanges.response.AssetCreatedResponse;
import com.crio.jumbotail.assettracking.repositories.AssetRepository;
import com.crio.jumbotail.assettracking.repositories.LocationDataRepository;
import com.crio.jumbotail.assettracking.service.AssetCreationServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.time.Instant;
import java.util.List;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

//@Disabled
@AutoConfigureMockMvc
@SpringBootTest
class DataCreationControllerTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	AssetRepository assetRepository;

	@Autowired
	AssetCreationServiceImpl assetCreationService;

	@Autowired
	LocationDataRepository locationDataRepository;

	@Autowired
	ObjectMapper objectMapper;

	GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);


	@Value("classpath:locations.csv")
	Resource resourceFile;


	@Test
	void test_asset_creation_returns_id_of_created_asset() throws Exception {

		AssetCreationRequest creationRequest = new AssetCreationRequest(
				RandomStringUtils.randomAlphabetic(10),
				RandomStringUtils.randomAlphabetic(10),
				new LocationDataDto(
						geometryFactory.createPoint(new Coordinate(23.0, 23.0)),
						Instant.now().getEpochSecond()
				),
				RandomStringUtils.randomAlphabetic(10)
		);

		createAsset(creationRequest)
				.andExpect(status().isCreated());

		final List<Asset> all = assetRepository.findAll();

		assertEquals(1, all.size());
	}

	@Test
	void test_asset_location_update() throws Exception {

		AssetCreationRequest creationRequest = new AssetCreationRequest(
				RandomStringUtils.randomAlphabetic(10),
				RandomStringUtils.randomAlphabetic(10),
				new LocationDataDto(
						geometryFactory.createPoint(new Coordinate(23.0, 23.0)),
						Instant.now().getEpochSecond()
				),
				RandomStringUtils.randomAlphabetic(10)
		);

		final String response = createAsset(creationRequest)
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();

		final Long id = objectMapper.readValue(response, AssetCreatedResponse.class).getId();

		updateAssetHistory(new LocationUpdateRequest(
				new LocationDataDto(geometryFactory.createPoint(new Coordinate(24.0, 24.0)),
						Instant.now().getEpochSecond())), id);

		final List<LocationData> all = locationDataRepository.findAll();

		assertEquals(2, all.size());
	}

	@SneakyThrows
	@Test
	void test_mock_data_api() {
		mockMvc.perform(get("/api/mock-data"))
				.andExpect(status().isOk());
	}
	@SneakyThrows
	@Test
	void test_mock_data_api_within_india() {
		mockMvc.perform(get("/api/mock-data?withinIndia=true"))
				.andExpect(status().isOk());
	}
	@SneakyThrows
	@Test
	void test_mock_data_api_file_mock() {
		mockMvc.perform(get("/api/mock-data?mockFromFile=true"))
				.andExpect(status().isOk());
	}

	@SneakyThrows
	@Test
	void test_mock_history_api() {
		mockMvc.perform(get("/api/mock-history?n=1"))
				.andExpect(status().isOk());
	}

	@Value("classpath:prep_data/assets/geofence.json")
	private Resource geofence;

	@Value("classpath:prep_data/assets/route.json")
	private Resource route;


	@SneakyThrows
	@Test
	void update_api_for_boundary_polygon() {
		AssetCreationRequest creationRequest = new AssetCreationRequest("", "",
				new LocationDataDto(geometryFactory.createPoint(new Coordinate(23.0, 23.0)),
						Instant.now().getEpochSecond()), "");

		final String response = createAsset(creationRequest)
				.andReturn().getResponse().getContentAsString();
		final Long id = objectMapper.readValue(response, AssetCreatedResponse.class).getId();

		final File geofenceFile = geofence.getFile();
		final String geofenceData = FileUtils.readFileToString(geofenceFile, "UTF-8");

		mockMvc.perform(post("/api/assets/" + id + "/POLYGON")
				.content(geofenceData)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated());
	}

	@SneakyThrows
	@Test
	void update_api_for_boundary_linestring() {
		AssetCreationRequest creationRequest = new AssetCreationRequest("", "",
				new LocationDataDto(geometryFactory.createPoint(new Coordinate(23.0, 23.0)),
						Instant.now().getEpochSecond()), "");

		final String response = createAsset(creationRequest)
				.andReturn().getResponse().getContentAsString();
		final Long id = objectMapper.readValue(response, AssetCreatedResponse.class).getId();
		final File routeFile = route.getFile();
		final String routeData = FileUtils.readFileToString(routeFile, "UTF-8");

		mockMvc.perform(post("/api/assets/" + id + "/LINESTRING")
				.content(routeData)
				.contentType(MediaType.APPLICATION_JSON)
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated());
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


	@AfterEach
	void tearDown() {
		locationDataRepository.deleteAll();
		locationDataRepository.flush();
		assertEquals(0, locationDataRepository.findAll().size());

		assetRepository.deleteAll();
		assetRepository.flush();
		assertEquals(0, assetRepository.findAll().size());
	}
}