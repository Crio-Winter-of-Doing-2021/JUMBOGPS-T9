package com.crio.jumbotail.assettracking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories
public class AssetTrackingApplication {

	public static void main(String[] args) {
		SpringApplication.run(AssetTrackingApplication.class, args);
	}


}
