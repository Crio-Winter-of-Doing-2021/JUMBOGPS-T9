package com.crio.jumbotail.assettracking.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


import com.crio.jumbotail.assettracking.repositories.UserRepository;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@WithMockUser(roles = "ADMIN")
@ActiveProfiles("inmem-cache")
@AutoConfigureMockMvc
@SpringBootTest
class AuthenticationIntegrationTest {

	@Autowired
	MockMvc mockMvc;

	@Autowired
	UserRepository userRepository;

	@SneakyThrows
	@Test
	@WithMockUser
	void access_to_api_with_auth() {
		mockMvc.perform(
				MockMvcRequestBuilders.get("/assets"))
				.andExpect(status().isOk());
	}

	@SneakyThrows
	@Test
	void access_to_auth_api_no_data_fails() {
		mockMvc.perform(
				MockMvcRequestBuilders.post("/authenticate"))
				.andExpect(status().is4xxClientError());
	}

	@SneakyThrows
	@Test
	void access_to_auth_api_wrong_cred_data_fails() {
		mockMvc.perform(
				post("/authenticate")
						.content("{\"username\": \"user\",\"password\": \"pass\"}")
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());

	}

	@SneakyThrows
	@Test
	@Sql(scripts = "classpath:prep_data/auth/data.sql")
	void access_to_auth_api_correct_cred_data_is_ok() {
		mockMvc.perform(
				post("/authenticate")
						.content("{\"username\": \"testuser\",\"password\": \"string\"}")
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

	}

	@SneakyThrows
	@Test
	void register_user_cannot_insert_duplicate_username() {
		mockMvc.perform(
				post("/register")
						.content("{\"username\": \"user\",\"password\": \"string\", \"role\" : \"ROLE_ADMIN\"}")
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated());

		mockMvc.perform(
				post("/register")
						.content("{\"username\": \"user\",\"password\": \"string\", \"role\" : \"ROLE_ADMIN\"}")
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());

	}


	@SneakyThrows
	@Test
	void register_user_work_with_diff_username() {
		mockMvc.perform(
				post("/register")
						.content("{\"username\": \"user\",\"password\": \"string\", \"role\" : \"ROLE_ADMIN\"}")
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated());

		mockMvc.perform(
				post("/register")
						.content("{\"username\": \"user1\",\"password\": \"string\", \"role\" : \"ROLE_ADMIN\"}")
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isCreated());

	}

	@SneakyThrows
	@Test
	void register_user_error_on_null_fields() {
		mockMvc.perform(
				post("/register")
						.content("{\"password\": \"string\", \"role\" : \"ROLE_ADMIN\"}")
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());

		mockMvc.perform(
				post("/register")
						.content("{\"username\": \"user1\",\"password\": \"string\", }")
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());

		mockMvc.perform(
				post("/register")
						.content("{\"username\": \"user1\", \"role\" : \"ROLE_ADMIN\"}")
						.contentType(MediaType.APPLICATION_JSON)
						.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest());
	}

	@Test
	void refresh_token() {

	}

	@AfterEach
	void tearDown() {
		userRepository.deleteAll();
		userRepository.flush();
		assertEquals(0, userRepository.findAll().size());
	}




}