package com.crio.jumbotail.assettracking.service;

import static java.time.ZoneId.systemDefault;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


import com.bedatadriven.jackson.datatype.jts.JtsModule;
import com.crio.jumbotail.assettracking.entity.Asset;
import com.crio.jumbotail.assettracking.entity.LocationData;
import com.crio.jumbotail.assettracking.exceptions.AssetNotFoundException;
import com.crio.jumbotail.assettracking.exceptions.InvalidLocationException;
import com.crio.jumbotail.assettracking.exchanges.request.AssetCreationRequest;
import com.crio.jumbotail.assettracking.exchanges.request.LocationDataDto;
import com.crio.jumbotail.assettracking.exchanges.request.LocationUpdateRequest;
import com.crio.jumbotail.assettracking.exchanges.response.AssetCreatedResponse;
import com.crio.jumbotail.assettracking.repositories.AssetRepository;
import com.crio.jumbotail.assettracking.repositories.LocationDataRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import javax.persistence.EntityNotFoundException;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith( {MockitoExtension.class, SpringExtension.class})
class AssetCreationServiceTest {

	GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);

	@MockBean
	private AssetBoundaryCacheService cacheService;

	@MockBean
	private LocationDataRepository locationDataRepository;

	@MockBean
	private AssetRepository assetRepository;

	@MockBean
	private AssetNotificationCreator notificationService;

	@InjectMocks
	AssetCreationService assetCreationService = new AssetCreationServiceImpl();


	@Test
	void correct_asset_id_returned_when_creating_asset() {

		final AssetCreationRequest assetCreationRequestMock = mock(AssetCreationRequest.class, Mockito.RETURNS_DEEP_STUBS);
		doReturn(null)
				.when(assetCreationRequestMock).getGeofence();
		doReturn(null)
				.when(assetCreationRequestMock).getRoute();

		Asset assetMock = mock(Asset.class);
		doReturn(1000L).when(assetMock).getId();

		doReturn(assetMock).when(assetRepository).save(any(Asset.class));

		final AssetCreatedResponse asset = assetCreationService.createAsset(assetCreationRequestMock);

		assertEquals(1000L, asset.getId());
	}

	@Test
	void fields_are_accurately_mapped_no_route_and_geofence() {

		// given
		final String title = "title";
		final String description = "description";
		final String assetType = "TRUCK";
		final AssetCreationRequest assetCreationRequest = new AssetCreationRequest(title, description,
				new LocationDataDto(
						mock(Point.class),
						0),
				assetType
		);

		Long expectedId = RandomUtils.nextLong();
		doAnswer(invocation -> {
			Asset toSave = invocation.getArgument(0);
			ReflectionTestUtils.setField(toSave, "id", expectedId);
			return toSave;
		}).when(assetRepository).save(any(Asset.class));

		// when
		assetCreationService.createAsset(assetCreationRequest);

		// then
		ArgumentCaptor<Asset> assetArgumentCaptor = ArgumentCaptor.forClass(Asset.class);
		verify(assetRepository, times(1))
				.save(assetArgumentCaptor.capture());
		final Asset createdAsset = assetArgumentCaptor.getValue();

		assertEquals(assetType, createdAsset.getAssetType());
		assertEquals(description, createdAsset.getDescription());
		assertEquals(title, createdAsset.getTitle());
		assertEquals(expectedId, createdAsset.getId());

	}

	@Test
	void fields_are_not_mapped_when_no_hibernate_or_jpa() {

		// given
		final AssetCreationRequest assetCreationRequest = new AssetCreationRequest(
				"", "", new LocationDataDto(mock(Point.class), 0), ""
		);

		doAnswer(invocation -> {
			Asset toSave = invocation.getArgument(0);
			ReflectionTestUtils.setField(toSave, "id", RandomUtils.nextLong());
			return toSave;
		}).when(assetRepository).save(any(Asset.class));

		// when
		assetCreationService.createAsset(assetCreationRequest);

		// then
		ArgumentCaptor<Asset> assetArgumentCaptor = ArgumentCaptor.forClass(Asset.class);
		verify(assetRepository, times(1))
				.save(assetArgumentCaptor.capture());
		final Asset createdAsset = assetArgumentCaptor.getValue();

		// are not set when JPA/Hibernate not initialized i.e the PreUpdate and PrePersist method is not called
		assertNull(createdAsset.getLastReportedCoordinates());
		assertNull(createdAsset.getLastReportedCoordinates());

	}

	@Value("classpath:prep_data/assets/geofence.json")
	private Resource geofence;

	@Value("classpath:prep_data/assets/route.json")
	private Resource route;

	private static final GeometryFactory gf = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);
	public static ObjectMapper objectmapper;

	@BeforeAll
	static void setup() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JtsModule(gf));
		objectmapper = mapper;
	}

	//	@Disabled
	@SneakyThrows
	@Test
	void fields_are_accurately_mapped_with_route_and_geofence() {

		// given
		final String title = "title";
		final String description = "description";
		final String assetType = "TRUCK";
		final File geofenceFile = geofence.getFile();
		final String geofenceData = FileUtils.readFileToString(geofenceFile, "UTF-8");
		final Geometry geofence = objectmapper.readValue(geofenceData, Geometry.class);
		final File routeFile = route.getFile();
		final String routeData = FileUtils.readFileToString(routeFile, "UTF-8");
		final Geometry route = objectmapper.readValue(routeData, Geometry.class);

		final AssetCreationRequest assetCreationRequest = new AssetCreationRequest(
				title, description,
				new LocationDataDto(mock(Point.class), 0),
				assetType,
				// not mocking geofence and route as was not able to successfully
				// mock SpatialUtils.validateGeometry() void
				geofence, route
		);

		Long expectedId = RandomUtils.nextLong();
		doAnswer(invocation -> {
			Asset toSave = invocation.getArgument(0);
			ReflectionTestUtils.setField(toSave, "id", expectedId);
			return toSave;
		}).when(assetRepository).save(any(Asset.class));

		// when
		assetCreationService.createAsset(assetCreationRequest);

		// then
		ArgumentCaptor<Asset> assetArgumentCaptor = ArgumentCaptor.forClass(Asset.class);
		verify(assetRepository, times(1))
				.save(assetArgumentCaptor.capture());
		final Asset createdAsset = assetArgumentCaptor.getValue();

		assertEquals(expectedId, createdAsset.getId());
		assertEquals(assetType, createdAsset.getAssetType());
		assertEquals(description, createdAsset.getDescription());
		assertEquals(title, createdAsset.getTitle());
		assertEquals(route, createdAsset.getRoute());
		assertEquals(geofence, createdAsset.getGeofence());
		assertEquals(1, createdAsset.getLocationHistory().size());
	}

	@SneakyThrows
	@Test
	void should_insert_location_history() {

		// given
		final AssetCreationRequest assetCreationRequest = new AssetCreationRequest(
				"", "",
				new LocationDataDto(mock(Point.class), 0),
				"",
				null, null
		);

		Long expectedId = RandomUtils.nextLong();
		doAnswer(invocation -> {
			Asset toSave = invocation.getArgument(0);
			ReflectionTestUtils.setField(toSave, "id", expectedId);
			return toSave;
		}).when(assetRepository).save(any(Asset.class));

		// when
		assetCreationService.createAsset(assetCreationRequest);

		// then
		ArgumentCaptor<Asset> assetArgumentCaptor = ArgumentCaptor.forClass(Asset.class);
		verify(assetRepository, times(1))
				.save(assetArgumentCaptor.capture());
		final Asset createdAsset = assetArgumentCaptor.getValue();

		assertEquals(1, createdAsset.getLocationHistory().size());
	}


	@Value("classpath:prep_data/assets/invalid_geofence.json")
	private Resource invalid_geofence;

	@Value("classpath:prep_data/assets/invalid_route.json")
	private Resource invalid_route;

//	@Disabled("running on local failing on GITHUB")
	@Test
	void test_invalid_geofence() throws Exception {
		final File geofenceFile = invalid_geofence.getFile();
		final String geofenceData = FileUtils.readFileToString(geofenceFile, "UTF-8");
		final Geometry geofence = objectmapper.readValue(geofenceData, Geometry.class);

		System.out.println(objectmapper.writerWithDefaultPrettyPrinter()
				.writeValueAsString(geofence));

		assertThrows(InvalidLocationException.class,
				() -> assetCreationService.createAsset(
						new AssetCreationRequest(
								"title", "description",
								new LocationDataDto(mock(Point.class), 0),
								"assetType",
								geofence,
								null
						)
				));
	}

//	@Disabled("running on local failing on GITHUB")
	@Test
	void test_invalid_route() throws Exception {
		final File routeFile = invalid_route.getFile();
		final String routeData = FileUtils.readFileToString(routeFile, "UTF-8");
		final Geometry route = objectmapper.readValue(routeData, Geometry.class);

		System.out.println(objectmapper.writerWithDefaultPrettyPrinter()
				.writeValueAsString(route));


		assertThrows(InvalidLocationException.class,
				() -> assetCreationService.createAsset(
						new AssetCreationRequest(
								"title", "description",
								new LocationDataDto(mock(Point.class), 0),
								"assetType",
								null,
								route
						)
				));
	}

	@Test
	void test_invalid_data_type_in_geofence() {
		assertThrows(InvalidLocationException.class,
				() -> assetCreationService.createAsset(
						new AssetCreationRequest(
								"title", "description",
								new LocationDataDto(mock(Point.class), 0),
								"assetType",
								geometryFactory.createPoint(), // wrong datatype
								null
						)
				));
	}

	@Test
	void test_invalid_data_type_in_route() {
		assertThrows(InvalidLocationException.class,
				() -> assetCreationService.createAsset(
						new AssetCreationRequest(
								"title", "description",
								new LocationDataDto(mock(Point.class), 0),
								"assetType",
								null,
								geometryFactory.createPoint()// wrong datatype
						)
				));
	}


	// TEST updateLocationDataForAsset

	@Test
	void exception_when_asset_not_found() {

		final long assetId = 1000L;
		doThrow(EntityNotFoundException.class)
				.when(assetRepository).getOne(assetId);

		assertThrows(AssetNotFoundException.class,
				() -> assetCreationService.updateLocationDataForAsset(
						new LocationUpdateRequest(new LocationDataDto(geometryFactory.createPoint(new Coordinate(0, 0)
						), 0))
						, assetId));

	}


	@Test
	void notification_service_is_called_to_validate_asset_location() {

		final long assetId = 1000L;
		doReturn(mock(Asset.class))
				.when(assetRepository).getOne(assetId);
		doReturn(mock(LocationData.class))
				.when(locationDataRepository).save(any(LocationData.class));
		doReturn(null)
				.when(cacheService).get(any());
		doReturn(Optional.empty())
				.when(assetRepository).getRouteForAsset(assetId);
		doReturn(Optional.empty())
				.when(assetRepository).getGeofenceForAsset(assetId);

		// when
		assetCreationService.updateLocationDataForAsset(
				new LocationUpdateRequest(
						new LocationDataDto(geometryFactory.createPoint(new Coordinate(0, 0)),
								0))
				, assetId);

		// then
		verify(notificationService, times(1))
				.validateAssetLocation(eq(assetId),
						any(Point.class),
						eq(Optional.empty()),
						eq(Optional.empty())
				);

	}

	@Test
	void fields_are_mapped_correctly() {

		// given
		final long assetId = 1000L;
		doNothing()
				.when(notificationService).validateAssetLocation(any(), any(), any(), any());
		final Asset mock = mock(Asset.class);
		doReturn(mock)
				.when(assetRepository).getOne(assetId);

		// when
		assetCreationService.updateLocationDataForAsset(
				new LocationUpdateRequest(
						new LocationDataDto(
								geometryFactory.createPoint(new Coordinate(0, 0)),
								0))
				, assetId);

		// then
		ArgumentCaptor<LocationData> captor = ArgumentCaptor.forClass(LocationData.class);
		verify(locationDataRepository, times(1)).save(captor.capture());
		final LocationData value = captor.getValue();
		assertNotNull(value.getAsset());
		assertEquals(mock, value.getAsset());
		assertEquals(0, value.getCoordinates().getCoordinate().getX());
		assertEquals(0, value.getCoordinates().getCoordinate().getY());
		assertNotNull(value.getTimestamp());
		assertEquals(LocalDateTime.ofInstant(Instant.ofEpochSecond(0), systemDefault()), value.getTimestamp());

	}

	@Test
	void no_exception_when_updating_location() {
		// given
		final long assetId = 1000L;
		doNothing()
				.when(notificationService)
				.validateAssetLocation(any(), any(), any(), any());
		final Asset mock = mock(Asset.class);
		doReturn(mock)
				.when(assetRepository).getOne(assetId);
		doReturn(mock(LocationData.class))
				.when(locationDataRepository).save(any(LocationData.class));

		// when
		// then
		assertDoesNotThrow(() -> assetCreationService.updateLocationDataForAsset(
				new LocationUpdateRequest(
						new LocationDataDto(
								geometryFactory.createPoint(new Coordinate(0, 0)),
								0))
				, assetId));

	}


}