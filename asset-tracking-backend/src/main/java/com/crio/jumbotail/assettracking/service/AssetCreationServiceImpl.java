package com.crio.jumbotail.assettracking.service;

import com.crio.jumbotail.assettracking.entity.Asset;
import com.crio.jumbotail.assettracking.entity.LocationData;
import com.crio.jumbotail.assettracking.exceptions.AssetNotFoundException;
import com.crio.jumbotail.assettracking.exceptions.InvalidLocationException;
import com.crio.jumbotail.assettracking.exchanges.request.AssetCreationRequest;
import com.crio.jumbotail.assettracking.exchanges.request.LocationUpdateRequest;
import com.crio.jumbotail.assettracking.exchanges.response.AssetCreatedResponse;
import com.crio.jumbotail.assettracking.repositories.AssetRepository;
import com.crio.jumbotail.assettracking.repositories.LocationDataRepository;
import com.crio.jumbotail.assettracking.utils.SpatialUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Stream;
import javax.persistence.EntityNotFoundException;
import lombok.extern.log4j.Log4j2;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class AssetCreationServiceImpl implements AssetCreationService {

	@Autowired
	private GeometryFactory geometryFactory;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private AssetBoundaryCacheService cacheService;

	@Autowired
	private LocationDataRepository locationDataRepository;

	@Autowired
	private AssetRepository assetRepository;

	@Autowired
	private AssetNotificationCreator notificationService;

	/**
	 * @see <a href="https://stackoverflow.com/questions/2155037/what-are-the-distance-units-in-com-vividsolutions-jts-geom-geometry-class#:~:text=1%20radian%20%3D%20180%20degrees%20divided,get%20distance%20between%20two%20points.">
	 * Unit of Distance in JTS SRID 4326
	 * </a>
	 */
	@Value("${route.padding.decimal.degrees:0}")
	private Double routePaddingInDecimalDegrees;


	@Override
	public AssetCreatedResponse createAsset(AssetCreationRequest assetCreationRequest) {

		LOG.debug("assetCreationRequest [{}]", assetCreationRequest);

		final Point coordinates = assetCreationRequest.getLocation().getCoordinates();
		final long deviceTimestamp = assetCreationRequest.getLocation().getDeviceTimestamp();

		Asset.AssetBuilder assetPartial = Asset.builder()
				.assetType(assetCreationRequest.getAssetType())
				.description(assetCreationRequest.getDescription())
				.title(assetCreationRequest.getTitle());

		final LocationData locationData = new LocationData(coordinates, deviceTimestamp);

		if (assetCreationRequest.getGeofence() != null) {
			SpatialUtils.validateGeometry(assetCreationRequest.getGeofence());
			LOG.info("GEOFENCE FOUND");
			final Geometry geofence = assetCreationRequest.getGeofence();
			if (geofence instanceof Polygon) {
				assetPartial.geofence(geofence);
				LOG.debug("GEOFENCE CREATED");
			} else {
				throw new InvalidLocationException("Provided Geofence is invalid");
			}
		}
		if (assetCreationRequest.getRoute() != null) {
			SpatialUtils.validateGeometry(assetCreationRequest.getRoute());
			LOG.info("ROUTE FOUND");
			final Geometry route = assetCreationRequest.getRoute();
			if (route instanceof LineString) {
				assetPartial.route(route);
				LOG.debug("ROUTE CREATED");
			} else {
				throw new InvalidLocationException("Provided Route is invalid");
			}
		}

		Asset asset = assetPartial.build();
		asset.addLocationHistory(locationData);

		final Asset savedAsset = assetRepository.save(asset);

		return new AssetCreatedResponse(savedAsset.getId());
	}

	@Override
	public void updateLocationDataForAsset(LocationUpdateRequest locationUpdateRequest, Long assetId) {
		LOG.info(locationUpdateRequest);
		SpatialUtils.validateGeometry(locationUpdateRequest.getLocation().getCoordinates());
		try {
			// find the proxy asset
			// will throw an exception if not present
			Asset assetProxy = assetRepository.getOne(assetId);

			// create instance of location data
			LocationData newLocationData = new LocationData(
					locationUpdateRequest.getLocation().getCoordinates(),
					locationUpdateRequest.getLocation().getDeviceTimestamp()
			);
			newLocationData.setAsset(assetProxy);

			LOG.info("update location {} for asset {}", newLocationData, assetId);

			locationDataRepository.save(newLocationData);

			notificationService.validateAssetLocation(
					assetId,
					locationUpdateRequest.getLocation().getCoordinates(),
					getRouteForAsset(assetId),
					getGeofenceForAsset(assetId)
			);

		} catch (EntityNotFoundException e) {
			throw new AssetNotFoundException("Asset with " + assetId + " not found.");
		}

	}

	private Optional<Geometry> getGeofenceForAsset(Long assetId) {

		return Stream.of(Optional.ofNullable(cacheService.get("geofence-" + assetId)), assetRepository.getGeofenceForAsset(assetId))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.findFirst();
	}

	private Optional<Geometry> getRouteForAsset(Long assetId) {
		return Stream.of(Optional.ofNullable(cacheService.get("route-" + assetId)), assetRepository.getRouteForAsset(assetId))
				.filter(Optional::isPresent)
				.map(Optional::get)
				.findFirst();
	}

	@Override
	public void addBoundaryToAsset(Long assetId, String boundaryType, String data) {
		Coordinate[] shell;

		try {
			final ArrayList<Coordinate> points = new ArrayList<>();
			if (data == null || data.isEmpty()) {
				points.add(new Coordinate(-10, -10));
				points.add(new Coordinate(-10, 10));
				points.add(new Coordinate(10, 10));
				points.add(new Coordinate(10, -10));
				points.add(new Coordinate(-10, -10));

				shell = points.toArray(new Coordinate[0]);

			} else {
				final Geometry geometry = objectMapper.readValue(data, Geometry.class);

				shell = geometry.getCoordinates();
			}

			Asset asset = assetRepository.getOne(assetId);
			if ("POLYGON".equalsIgnoreCase(boundaryType)) {
				final Geometry geofence = geometryFactory.createPolygon(shell);

				asset.setGeofence(geofence);
			} else if ("LINESTRING".equalsIgnoreCase(boundaryType)) {
				Geometry route = geometryFactory.createLineString(shell);

				if (routePaddingInDecimalDegrees != 0) {
					route = route.buffer(routePaddingInDecimalDegrees);
				}

				asset.setRoute(route);
			}

			assetRepository.save(asset);


		} catch (JsonProcessingException e) {
			LOG.error(e);
			throw new IllegalArgumentException(e);
		}

	}

}
