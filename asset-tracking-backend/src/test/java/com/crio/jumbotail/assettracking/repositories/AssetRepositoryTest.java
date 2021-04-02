package com.crio.jumbotail.assettracking.repositories;

import static java.time.LocalDateTime.now;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;

@Disabled
@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class AssetRepositoryTest {

	@Autowired
	AssetRepository assetRepository;

	@BeforeEach
	void setUp() {
	}

	@AfterEach
	void tearDown() {
	}

	@Test
	void doAllQueries() {
		final Pageable unpaged = PageRequest.of(0, 100);
		assetRepository.findAllByAssetTypeOrderByLastReportedTimestampDesc("", unpaged);
		assetRepository.findAllByOrderByLastReportedTimestampDesc(unpaged);
		assetRepository.findAllByLastReportedTimestampBetween(now(), now(), unpaged);
		assetRepository.findAllByAssetTypeAndLastReportedTimestampBetween("", now(), now(), unpaged);
	}

}