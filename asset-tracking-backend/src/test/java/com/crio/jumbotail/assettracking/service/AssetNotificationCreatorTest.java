package com.crio.jumbotail.assettracking.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


import com.bedatadriven.jackson.datatype.jts.JtsModule;
import com.crio.jumbotail.assettracking.exchanges.response.Notification;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.text.MessageFormat;
import java.util.Optional;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith( {MockitoExtension.class, SpringExtension.class})
class AssetNotificationCreatorTest {

	@Mock
	public ApplicationEventPublisher eventPublisher;

	@Value("classpath:prep_data/assets/geofence.json")
	private Resource geofence;

	@Value("classpath:prep_data/assets/route.json")
	private Resource route;

	@InjectMocks
	AssetNotificationCreator notificationCreator = new AssetNotificationCreatorImpl();

	private static final GeometryFactory gf = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);
	public static ObjectMapper objectmapper;

	@BeforeAll
	static void setup() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JtsModule(gf));
		objectmapper = mapper;
	}

	@SneakyThrows
	@Test
	void should_notify_when_asset_outside_geofence() {
		Long assetId = 1000L;
		// given
		doNothing()
				.when(eventPublisher)
				.publishEvent(any(Notification.class));

		final File file = geofence.getFile();
		final String geofenceData = FileUtils.readFileToString(file, "UTF-8");
		final Geometry geofence = objectmapper.readValue(geofenceData, Geometry.class);


		// when
		notificationCreator
				.validateAssetLocation(
						assetId,
						gf.createPoint(new Coordinate(80.949363, 26.838478)),
						Optional.empty(),
						Optional.of(geofence));


		// then
		verify(eventPublisher, times(1))
				.publishEvent(any(Notification.class));
	}

	@SneakyThrows
	@Test
	void should_notify_when_asset_outside_route() {
		Long assetId = 1000L;
		// given
		doNothing()
				.when(eventPublisher)
				.publishEvent(any(Notification.class));

		final File file = route.getFile();
		final String routeData = FileUtils.readFileToString(file, "UTF-8");
		final Geometry routeGeometry = objectmapper.readValue(routeData, Geometry.class);

		// when
		notificationCreator
				.validateAssetLocation(
						assetId,
						gf.createPoint(new Coordinate(80.92550141046553, 26.83651252540345)),
						Optional.of(routeGeometry),
						Optional.empty());


		// then
		verify(eventPublisher, times(1))
				.publishEvent(any(Notification.class));
	}

	@SneakyThrows
	@Test
	void should_notify_when_asset_outside_geofence_and_route() {
		Long assetId = 1000L;
		// given
		doNothing()
				.when(eventPublisher)
				.publishEvent(any(Notification.class));

		final File geofenceFile = geofence.getFile();
		final String geofenceData = FileUtils.readFileToString(geofenceFile, "UTF-8");
		final Geometry geofence = objectmapper.readValue(geofenceData, Geometry.class);
		final File routeFile = route.getFile();
		final String routeData = FileUtils.readFileToString(routeFile, "UTF-8");
		final Geometry route = objectmapper.readValue(routeData, Geometry.class);


		// when
		notificationCreator
				.validateAssetLocation(
						assetId,
						gf.createPoint(new Coordinate(80.949363, 26.838478)),
						Optional.of(route),
						Optional.of(geofence));

		// then
		verify(eventPublisher, times(2))
				.publishEvent(any(Notification.class));
	}

	@SneakyThrows
	@Test
	void should_not_notify_when_asset_inside_geofence_and_route() {
		Long assetId = 1000L;
		final File geofenceFile = geofence.getFile();
		final String geofenceData = FileUtils.readFileToString(geofenceFile, "UTF-8");
		final Geometry geofence = objectmapper.readValue(geofenceData, Geometry.class);
		final File routeFile = route.getFile();
		final String routeData = FileUtils.readFileToString(routeFile, "UTF-8");
		final Geometry route = objectmapper.readValue(routeData, Geometry.class);

		// when
		notificationCreator
				.validateAssetLocation(
						assetId,
						gf.createPoint(new Coordinate(80.916357, 26.836392)),
						Optional.of(route),
						Optional.of(geofence));


		// then
		verify(eventPublisher, times(0))
				.publishEvent(any(Notification.class));
	}

	@SneakyThrows
	@Test
	void should_notify_with_valid_data() {
		Long assetId = 1000L;
		// given
		ArgumentCaptor<Notification> valueCapture = ArgumentCaptor.forClass(Notification.class);
		doNothing()
				.when(eventPublisher)
				.publishEvent(valueCapture.capture());

		final File file = geofence.getFile();
		final String geofenceData = FileUtils.readFileToString(file, "UTF-8");
		final Geometry geofence = objectmapper.readValue(geofenceData, Geometry.class);


		// when
		notificationCreator
				.validateAssetLocation(
						assetId,
						gf.createPoint(new Coordinate(80.949363, 26.838478)),
						Optional.empty(),
						Optional.of(geofence));


		// then
		assertEquals(assetId, valueCapture.getValue().getAssetId());
		assertEquals("geofence-exit", valueCapture.getValue().getEventType());
		assertEquals(MessageFormat.format("Asset {0} is outside defined geofence", String.valueOf(assetId)), valueCapture.getValue().getMessage());
	}


}