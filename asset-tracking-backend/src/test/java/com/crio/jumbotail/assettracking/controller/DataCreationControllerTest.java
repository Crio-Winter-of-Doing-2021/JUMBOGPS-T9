package com.crio.jumbotail.assettracking.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;


import com.crio.jumbotail.assettracking.entity.Asset;
import com.crio.jumbotail.assettracking.exchanges.AssetCreationRequest;
import com.crio.jumbotail.assettracking.exchanges.LocationDataDto;
import com.crio.jumbotail.assettracking.exchanges.LocationDto;
import com.crio.jumbotail.assettracking.repositories.AssetRepository;
import com.crio.jumbotail.assettracking.repositories.LocationDataRepository;
import com.crio.jumbotail.assettracking.service.AssetCreationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
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
	AssetCreationService assetCreationService;

	@Autowired
	LocationDataRepository locationDataRepository;

	@Autowired
	ObjectMapper objectMapper;

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