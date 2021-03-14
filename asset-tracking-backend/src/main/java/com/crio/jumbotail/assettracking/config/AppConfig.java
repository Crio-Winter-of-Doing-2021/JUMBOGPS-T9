package com.crio.jumbotail.assettracking.config;

import com.bedatadriven.jackson.datatype.jts.JtsModule;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import java.util.Collections;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * To Inject Bean Dependencies Needed by other classes
 */
@Getter
@Setter
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories
@EnableJpaAuditing
@Log4j2
public class AppConfig {

	@Value("${spring.profiles.active:na}")
	private String activeProfile;

	@Bean
	public Jdk8Module jdk8Module() {
		return new Jdk8Module();
	}

	@Bean
	public JavaTimeModule javaTimeModule() {
		return new JavaTimeModule();
	}

	@Bean
	public Hibernate5Module hibernate5Module() {
		return new Hibernate5Module();
	}

	@Bean
	public JtsModule jtsModule() {
		return new JtsModule(new GeometryFactory(new PrecisionModel(), 4326));
	}

	/**
	 * @return instance of model mapper
	 */
	@Bean
	public ModelMapper modelMapper() {
		final ModelMapper modelMapper = new ModelMapper();
		modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
		return modelMapper;
	}

	/**
	 * @return OPEN API documentation
	 */
	@Bean
	public OpenAPI openAPI() {
		final OpenAPI openAPI = new OpenAPI().info(new Info()
				.title("Asset Tracking application")
				.termsOfService("http://swagger.io/terms/")
				.version("1")
				.license(new License().name("Apache 2.0").url("http://springdoc.org")));
		if (activeProfile.contains("repl")) {
			openAPI.servers(Collections.singletonList(new Server().url("https://jumbogps.anugrahsinghal.repl.co/")));
		}
		return openAPI;

	}

	/**
	 * Config to Inject CorsBean to enable client-server communication
	 */
	@Bean
	public FilterRegistrationBean<CorsFilter> corsFilterRegistrationBean() {

		FilterRegistrationBean<CorsFilter> filterRegistrationBean = new FilterRegistrationBean<>();
		filterRegistrationBean.setFilter(new CorsFilter());
		filterRegistrationBean.setOrder(100);
		filterRegistrationBean.setName("CorsFilter");

		return filterRegistrationBean;
	}




}

