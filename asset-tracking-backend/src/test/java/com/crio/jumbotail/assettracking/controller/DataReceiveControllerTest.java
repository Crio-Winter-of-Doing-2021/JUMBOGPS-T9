package com.crio.jumbotail.assettracking.controller;

import com.crio.jumbotail.assettracking.exchanges.AssetCreationRequest;
import com.crio.jumbotail.assettracking.exchanges.Location;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@WebMvcTest
class DataReceiveControllerTest {

	@Autowired
	MockMvc mockMvc;

	public static String asJsonString(final Object obj) {
		try {
			return new ObjectMapper().writeValueAsString(obj);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	void test_asset_creation_returns_id_of_created_asset() throws Exception {
		AssetCreationRequest creationRequest = new AssetCreationRequest();
		creationRequest.setDescription("NA");
		Location location = new Location();
		location.setLatitude(23);
		location.setLongitude(23);
		creationRequest.setLocation(location);
		creationRequest.setDescription("NA");

		mockMvc
				.perform(
						MockMvcRequestBuilders
								.post("/assets")
								.content(asJsonString(creationRequest))
								.contentType(MediaType.APPLICATION_JSON)
								.accept(MediaType.APPLICATION_JSON)
				).andExpect(MockMvcResultMatchers.status().isCreated());
	}

}