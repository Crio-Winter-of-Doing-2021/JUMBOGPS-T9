package com.crio.jumbotail.assettracking.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
		name = "bearerAuth",
		type = SecuritySchemeType.HTTP,
		bearerFormat = "JWT",
		scheme = "bearer"
)
public class OpenApiConfig {

	@Value("${spring.profiles.active:na}")
	private String activeProfile;

	/**
	 * @return OPEN API documentation
	 */
	@Bean
	public OpenAPI openAPI() {
		final OpenAPI openAPI = new OpenAPI()
				.info(new Info()
						.title("Asset Tracking application")
						.description("API to Track Asset Movement.\nFor Testing Purposes use JWT Token :\n"
						             + "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhbnUiLCJzY29wZXMiOlsiUk9MRV9BRE1JTiJdLCJpYXQiOjE2MTYwODcxNjYsImV4cCI6MTYxODA4NzE2Nn0.xTM2kH7HPx5GpoGbtpftOkg3iStjhSjkn77CPn5Q5LR3SjP5-4nbxRL4HPynEauInM49OvJlyvNAspyWy_FhgQ")
						.termsOfService("http://swagger.io/terms/")
						.version("1")
						.license(new License().name("Apache 2.0").url("http://springdoc.org"))
				);
		if (activeProfile.contains("repl")) {
			openAPI.servers(Collections.singletonList(new Server().url("https://jumbogps.anugrahsinghal.repl.co/")));
		}
		return openAPI;

	}

}
