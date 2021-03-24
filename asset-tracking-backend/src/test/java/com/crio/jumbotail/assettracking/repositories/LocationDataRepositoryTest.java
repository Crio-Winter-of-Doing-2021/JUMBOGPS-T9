package com.crio.jumbotail.assettracking.repositories;

import static java.time.temporal.ChronoUnit.HOURS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


import com.crio.jumbotail.assettracking.entity.Asset;
import com.crio.jumbotail.assettracking.entity.LocationData;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
class LocationDataRepositoryTest {

	@Autowired
	private LocationDataRepository locationDataRepository;

	// USED TO ADJUST AGAINST TIMESTAMPS FOR DB AND JVM
	private static final ZoneOffset offset = OffsetDateTime.now().getOffset();

	GeometryFactory geometryFactory = new GeometryFactory(
			new PrecisionModel(PrecisionModel.FLOATING),
			4326
	);

	@Test
	void contextLoads() {
		assertNotNull(locationDataRepository);
	}

	@Test
	void location_data_cannot_exist_without_asset() {

		assertThrows(RuntimeException.class, () -> locationDataRepository.save(new LocationData(
						geometryFactory.createPoint(),
						Instant.EPOCH.getEpochSecond()
				))
		);

	}

	@Test
	void location_data_not_work_for_NULL_COORDINATES_data() {

		assertThrows(RuntimeException.class, () -> locationDataRepository.save(new LocationData(
						null,
						Instant.EPOCH.getEpochSecond()
				))
		);

	}

	@Test
	void location_data_not_work_for_NULL_TIME_data() {

		assertThrows(RuntimeException.class, () -> locationDataRepository.save(new LocationData(
						geometryFactory.createPoint(),
						(Long) null
				))
		);

	}

	@Test
	void location_data_not_work_for_NULL_TIME_data_2() {

		assertThrows(RuntimeException.class, () -> locationDataRepository.save(new LocationData(
						geometryFactory.createPoint(),
						(LocalDateTime) null
				))
		);

	}


	@Test
	void location_data__work_for_valid_data() {


		Asset asset = mock(Asset.class);

		final LocationData entity = new LocationData(
				geometryFactory.createPoint(),
				Instant.EPOCH.getEpochSecond()
		);
		entity.setAsset(asset);
		assertDoesNotThrow(() -> {
					locationDataRepository.save(entity);
				}
		);


	}

	@Test
	void location_data_work_for_valid_data_and_update_prepersist_preupdate_method_is_called() {

		Asset asset = mock(Asset.class);

		final LocationData entity = new LocationData(
				geometryFactory.createPoint(),
				Instant.EPOCH.getEpochSecond()
		);
		entity.setAsset(asset);
		assertDoesNotThrow(() -> {
					locationDataRepository.save(entity);
				}
		);

		verify(asset, times(1)).setLastReportedTimestamp(any(LocalDateTime.class));
		verify(asset, times(1)).setLastReportedCoordinates(any(Point.class));

	}

	@Test
	@Sql(scripts = "classpath:prep_data/location_data/some_location_data.sql")
	void test_data_is_fetched_for_last_24_hours_only()/* throws JsonProcessingException */ {

//		ObjectMapper mapper = new ObjectMapper();
//		mapper.registerModule(new JtsModule(new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326)));
//		System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(
//				locationDataRepository.findAll()));

		LocalDateTime highestDateOfPrepData = LocalDateTime.of(2021, Month.MARCH, 24, 22, 48, 7);
		final int totalSeconds = offset.getTotalSeconds();
		final LocalDateTime actualDateTime = highestDateOfPrepData.plusSeconds(totalSeconds);


		final List<LocationData> locations = locationDataRepository.findAllByAsset_IdAndTimestampBetweenOrderByTimestampDesc(
				3000L,
				actualDateTime.minus(24, HOURS),
				actualDateTime);

		assertEquals(14, locations.size());

	}

	@Test
	@Sql(scripts = "classpath:prep_data/location_data/some_location_data_one_outlier.sql")
	void test_data_is_fetched_for_last_24_hours_only_edge_case() /*throws JsonProcessingException */ {

//		ObjectMapper mapper = new ObjectMapper();
//		mapper.registerModule(new JtsModule(new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326)));
//		System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(
//				locationDataRepository.findAll()));

		LocalDateTime highestDateOfPrepData = LocalDateTime.of(2021, Month.MARCH, 24, 22, 48, 7);
		final int totalSeconds = offset.getTotalSeconds();
		final LocalDateTime actualDateTime = highestDateOfPrepData.plusSeconds(totalSeconds);

		final List<LocationData> locations = locationDataRepository.findAllByAsset_IdAndTimestampBetweenOrderByTimestampDesc(
				3000L,
				actualDateTime.minus(24, HOURS),
				actualDateTime);

		assertEquals(14, locations.size());

	}

	@Test
	@Sql(scripts = "classpath:prep_data/location_data/some_location_data_many_outlier.sql")
	void test_data_is_fetched_for_last_24_hours_only_edge_case_multiple_outlier() {

		LocalDateTime highestDateOfPrepData = LocalDateTime.of(2021, Month.MARCH, 24, 22, 48, 7);
		final int totalSeconds = offset.getTotalSeconds();
		final LocalDateTime actualDateTime = highestDateOfPrepData.plusSeconds(totalSeconds);

		final List<LocationData> locations = locationDataRepository.findAllByAsset_IdAndTimestampBetweenOrderByTimestampDesc(
				3000L,
				actualDateTime.minus(24, HOURS),
				actualDateTime);

		assertEquals(14, locations.size());

	}


	@Test
	@Sql(scripts = "classpath:prep_data/location_data/some_location_data.sql")
	void test_data_is_sorted() {

		LocalDateTime highestDateOfPrepData = LocalDateTime.of(2021, Month.MARCH, 24, 22, 48, 7);
		final int totalSeconds = offset.getTotalSeconds();
		final LocalDateTime actualDateTime = highestDateOfPrepData.plusSeconds(totalSeconds);

		final List<LocationData> locations = locationDataRepository.findAllByAsset_IdAndTimestampBetweenOrderByTimestampDesc(
				3000L,
				actualDateTime.minus(24, HOURS),
				actualDateTime);

		final List<LocalDateTime> dateList = locations.stream().map(LocationData::getTimestamp).sorted(Comparator.reverseOrder()).collect(Collectors.toList());


		assertThat(dateList,
				IsIterableContainingInOrder.contains(locations.stream().map(LocationData::getTimestamp).toArray()));

	}


	@Test
	@Sql(scripts = "classpath:prep_data/location_data/some_location_data.sql")
	void no_data_returned_if_not_within_24_hours() {

		// note that here year is 2022 whereas in data it is 2021
		LocalDateTime highestDateOfPrepData = LocalDateTime.of(2022, Month.MARCH, 24, 22, 48, 7);
		final int totalSeconds = offset.getTotalSeconds();
		final LocalDateTime actualDateTime = highestDateOfPrepData.plusSeconds(totalSeconds);

		final List<LocationData> locations = locationDataRepository.findAllByAsset_IdAndTimestampBetweenOrderByTimestampDesc(
				3000L,
				actualDateTime.minus(24, HOURS),
				actualDateTime);

		assertEquals(0, locations.size());
	}

	@Test
	void no_data_returned_if_not_present() {

		// note that here year is 2022 whereas in data it is 2021
		LocalDateTime highestDateOfPrepData = LocalDateTime.of(2021, Month.MARCH, 24, 22, 48, 7);
		final int totalSeconds = offset.getTotalSeconds();
		final LocalDateTime actualDateTime = highestDateOfPrepData.plusSeconds(totalSeconds);

		final List<LocationData> locations = locationDataRepository.findAllByAsset_IdAndTimestampBetweenOrderByTimestampDesc(
				3000L,
				actualDateTime.minus(24, HOURS),
				actualDateTime);

		assertEquals(0, locations.size());
	}

}