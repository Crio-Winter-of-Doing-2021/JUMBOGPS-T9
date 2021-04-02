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

//	@Bean
//	TopicExchange exchange() {
//		return new TopicExchange(TOPIC_EXCHANGE_NAME);
//	}
//
//	@Bean
//	ConnectionFactory connectionFactory() {
//		final CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory();
//		cachingConnectionFactory.setUri(uri);
//		return cachingConnectionFactory;
//	}
//
//
//	public static final String TOPIC_EXCHANGE_NAME = "spring-boot-exchange-topic";
//
//	static final String QUEUE_NAME = "spring-boot";
//
//	@Value(value = "${rabbit.uri}")
//	String uri;
//

}
