package com.crio.jumbotail.assettracking.config;

import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import java.io.IOException;
import java.util.Collections;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.filter.OncePerRequestFilter;

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
	public Hibernate5Module hibernate5Module() {
		return new Hibernate5Module();
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
	/*https://www.dariawan.com/tutorials/spring/documenting-spring-boot-rest-api-springdoc-openapi-3/*/

	/**
	 * @return OPEN API documentation
	 */
	@Bean
	public OpenAPI customOpenAPI() {
		final OpenAPI asset_tracking_xapplication = new OpenAPI().info(new Info()
				.title("Asset Tracking xapplication")
				.termsOfService("http://swagger.io/terms/")
				.version("1")
				.license(new License().name("Apache 2.0").url("http://springdoc.org")));
		if(activeProfile.equalsIgnoreCase("repl")) {
			asset_tracking_xapplication.servers(Collections.singletonList(new Server().url("https://jumbogps.anugrahsinghal.repl.co/")));
		}
			return asset_tracking_xapplication;

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

	/**
	 * Config Class To enable CORS access
	 */
	private class CorsFilter extends OncePerRequestFilter {

		@Override
		protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
			LOG.debug("CORS REQUEST came here outer");
			response.setHeader("Access-Control-Allow-Methods", "GET, PATCH, POST, PUT, DELETE, OPTIONS");
			response.setHeader("Access-Control-Allow-Origin", "*");
			response.setHeader("Access-Control-Max-Age", "3600");
			response.setHeader("Access-Control-Allow-Headers", "*");
			response.addHeader("Access-Control-Expose-Headers", "*");
			if ("OPTIONS".equals(request.getMethod())) {
				LOG.debug("CORS REQUEST came here");
				response.setStatus(HttpServletResponse.SC_OK);
				response.setHeader("Access-Control-Allow-Methods", "GET, PATCH, POST, PUT, DELETE, OPTIONS");
				LOG.debug(response);
			} else {
				filterChain.doFilter(request, response);
			}
		}
	}


}

