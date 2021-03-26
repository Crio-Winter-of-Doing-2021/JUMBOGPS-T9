package com.crio.jumbotail.assettracking.config;

import com.bedatadriven.jackson.datatype.jts.JtsModule;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.concurrent.Executor;
import lombok.extern.log4j.Log4j2;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * To Inject Bean Dependencies Needed by other classes
 */
//@EnableJpaAuditing
@Configuration
@EnableTransactionManagement
@EnableAsync
@Log4j2
public class AppConfig {

	/**
	 * @return Module for Jdk8 support for Jackson
	 */
	@Bean
	public Jdk8Module jdk8Module() {
		return new Jdk8Module();
	}

	/**
	 * @return Module for Jdk8 Time Libraries support for Jackson
	 */
	@Bean
	public JavaTimeModule javaTimeModule() {
		return new JavaTimeModule();
	}

	/**
	 * @return Module to stop serialization by object mapper of lazy loaded entities
	 */
	@Bean
	public Hibernate5Module hibernate5Module() {
		return new Hibernate5Module();
	}

	/**
	 * @return Module for geometry types de/serialization
	 */
	@Bean
	public JtsModule jtsModule() {
		return new JtsModule(geometryFactory());
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

	@Bean
	public GeometryFactory geometryFactory() {
		return new GeometryFactory(
				new PrecisionModel(PrecisionModel.FLOATING),
				4326);
	}

	@Bean(name = "notificationThreadPoolTaskExecutor")
	public Executor notificationThreadPoolTaskExecutor() {
		return new ThreadPoolTaskExecutor();
	}


}

