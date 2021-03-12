package com.crio.jumbotail.assettracking.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
import com.crio.jumbotail.assettracking.service.AssetCreationServiceImpl;
import com.crio.jumbotail.assettracking.testutils.TestUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
//@WebMvcTest
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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

	@Value("classpath:locations.csv")
	Resource resourceFile;

	TestUtils utils = new TestUtils();

	public static String asJsonString(final Object obj) {
		try {
			return new ObjectMapper().writeValueAsString(obj);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	void test_asset_creation_returns_id_of_created_asset() throws Exception {

		AssetCreationRequest creationRequest = new AssetCreationRequest(
				RandomStringUtils.randomAlphabetic(10),
				RandomStringUtils.randomAlphabetic(10),
				new LocationDataDto(
						new LocationDto(23.0, 23.0),
						Instant.now().getEpochSecond()
				),
				RandomStringUtils.randomAlphabetic(10)
		);

		mockMvc
				.perform(
						MockMvcRequestBuilders
								.post("/assets")
								.content(asJsonString(creationRequest))
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
				).andExpect(MockMvcResultMatchers.status().isCreated());

		final List<Asset> all = assetRepository.findAll();

		System.out.println(objectMapper.writeValueAsString(all));

		assertEquals(1, all.size());
	}

	@Test
	@Disabled
	void location_is_updated() throws Exception {

		final File file = resourceFile.getFile();
		String data = FileUtils.readFileToString(file, "UTF-8");
		List<String> locations = Arrays.asList(data.split("\n"));
		final String[] s = locations.get(0).split("\t");
		assert (s.length == 2);

		AssetCreationRequest assetCreationRequest = new AssetCreationRequest(
				RandomStringUtils.randomAlphabetic(10),
				RandomStringUtils.randomAlphabetic(40),
				new LocationDataDto(
						new LocationDto(Double.valueOf(s[0]), Double.valueOf(s[1])),
						LocalDateTime.now().plus(10000, ChronoUnit.SECONDS).toEpochSecond(ZoneOffset.UTC)),
				"TRUCK");

		final MvcResult mvcResult = mockMvc.perform(
				post("/assets")
						.content(asJsonString(assetCreationRequest))
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)

		)
				.andExpect(MockMvcResultMatchers.status().isCreated())
				.andReturn();

		final String responseStr = mvcResult.getResponse().getContentAsString();

		final AssetCreatedResponse assetCreatedResponse = objectMapper.readValue(responseStr, AssetCreatedResponse.class);


		final MvcResult result = mockMvc.perform(get("/assets/" + assetCreatedResponse.getId()))
				.andExpect(status().isOk())
				.andDo(print())
//				.andExpect(jsonPath("$.id", Matchers.))
				.andReturn();

		final LocationData[] locationDataArr = objectMapper.readValue(result.getResponse().getContentAsString(), LocationData[].class);

		final LocationDto locationDtoUpdated = utils.addMetersToCurrent(locationDataArr[0].getLocation(), 5000);

		LocationUpdateRequest updateRequest = new LocationUpdateRequest();
		updateRequest.setId(assetCreatedResponse.getId());
		updateRequest.setLocation(new LocationDataDto(locationDtoUpdated, LocalDateTime.now().plus(10000, ChronoUnit.SECONDS).toEpochSecond(ZoneOffset.UTC)));

		final MvcResult mvcResultUpdate = mockMvc.perform(
				patch("/assets/" + assetCreatedResponse.getId())
						.content(asJsonString(updateRequest))
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON)

		)
				.andExpect(jsonPath("$", hasSize(2)))
				.andReturn();

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