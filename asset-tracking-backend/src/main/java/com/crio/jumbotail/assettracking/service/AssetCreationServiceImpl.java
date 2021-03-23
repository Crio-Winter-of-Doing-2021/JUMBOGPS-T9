package com.crio.jumbotail.assettracking.service;

import com.crio.jumbotail.assettracking.entity.Asset;
import com.crio.jumbotail.assettracking.entity.Location;
import com.crio.jumbotail.assettracking.entity.LocationData;
import com.crio.jumbotail.assettracking.exceptions.AssetNotFoundException;
import com.crio.jumbotail.assettracking.exchanges.request.AssetCreationRequest;
import com.crio.jumbotail.assettracking.exchanges.request.LocationUpdateRequest;
import com.crio.jumbotail.assettracking.exchanges.response.AssetCreatedResponse;
import com.crio.jumbotail.assettracking.repositories.AssetRepository;
import com.crio.jumbotail.assettracking.repositories.LocationDataRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.persistence.EntityNotFoundException;
import lombok.extern.log4j.Log4j2;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class AssetCreationServiceImpl implements AssetCreationService {

	public static final GeometryFactory gf = new GeometryFactory(new PrecisionModel(), 4326);
	private final Map<Long, Polygon> assetGeofenceCache = new ConcurrentHashMap<>();
	private final Map<Long, LineString> assetRouteCache = new ConcurrentHashMap<>();


	@Autowired
	private LocationDataRepository locationDataRepository;

	@Autowired
	private AssetRepository assetRepository;

	@Autowired
	private ModelMapper modelMapper;

	@Autowired
	private AssetNotificationCreator notificationService;

	@Override
	public AssetCreatedResponse createAsset(AssetCreationRequest assetCreationRequest) {
		LOG.debug("assetCreationRequest [{}]", assetCreationRequest);

		final Location location = modelMapper.map(assetCreationRequest.getLocation().getLocationDto(), Location.class);
		LOG.debug("MODEL MAPPER location [{}]", location);
		final LocationData locationData = new LocationData(location, assetCreationRequest.getLocation().getDeviceTimestamp());

		Asset.AssetBuilder assetPartial = Asset.builder()
				.assetType(assetCreationRequest.getAssetType())
				.description(assetCreationRequest.getDescription())
				.title(assetCreationRequest.getTitle())
				.lastReportedTimestamp(locationData.getTimestamp())
				.lastReportedLocation(locationData.getLocation());

		if (assetCreationRequest.getGeofence() != null) {
			assetPartial.geofence(gf.createPolygon());
		}
		if (assetCreationRequest.getRoute() != null) {
			assetPartial.route(gf.createLineString());
		}

		Asset asset = assetPartial.build();
		asset.addLocationHistory(locationData);

		final Asset savedAsset = assetRepository.save(asset);

		return new AssetCreatedResponse(savedAsset.getId());
	}

	@Override
	public void updateLocationDataForAsset(LocationUpdateRequest locationUpdateRequest, Long assetId) {
		try {
			// find the proxy asset
			// will throw an exception if not present
			Asset assetProxy = assetRepository.getOne(assetId);

			// create instance of location to hold the co-ordinates
			Location location = modelMapper.map(locationUpdateRequest.getLocation().getLocationDto(), Location.class);
			// create instance of location data
			LocationData newLocationData = new LocationData(
					location,
					locationUpdateRequest.getLocation().getDeviceTimestamp()
			);
			newLocationData.setAsset(assetProxy);


			locationDataRepository.save(newLocationData);

			notificationService.validateAssetLocation(
					assetId,
					location,
					getRouteForAsset(assetId),
					getGeofenceForAsset(assetId)
			);

		} catch (EntityNotFoundException e) {
			throw new AssetNotFoundException("Asset with " + assetId + " not found.");
		}

	}

	private Polygon getGeofenceForAsset(Long assetId) {
		return assetGeofenceCache.computeIfAbsent(assetId, id -> assetRepository.getGeofenceForAsset(id));
	}

	private LineString getRouteForAsset(Long assetId) {
		return assetRouteCache.computeIfAbsent(assetId, id -> assetRepository.getRouteForAsset(id));
	}

	@Autowired
	private ObjectMapper objectMapper;

	@Override
	public void addBoundaryToAsset(Long assetId, String boundaryType, String data) {

		try {
			final ArrayList<Coordinate> points = new ArrayList<>();
			if(data==null || data.isEmpty()) {
				points.add(new Coordinate(-10, -10));
				points.add(new Coordinate(-10, 10));
				points.add(new Coordinate(10, 10));
				points.add(new Coordinate(10, -10));
				points.add(new Coordinate(-10, -10));

			} else {
				final Double[][] coordinates = objectMapper.readValue(data, Double[][].class);

				for (final Double[] coordinate : coordinates) {
					Coordinate c = new Coordinate(coordinate[0], coordinate[1]);
					points.add(c);
				}

			}
			final Coordinate[] shell = points.toArray(new Coordinate[0]);

			Asset asset = assetRepository.getOne(assetId);
			if ("POLYGON".equalsIgnoreCase(boundaryType)) {
				final Polygon geofence = gf.createPolygon(shell);
				asset.setGeofence(geofence);
			} else if ("LINESTRING".equalsIgnoreCase(boundaryType)) {
				final LineString route = gf.createLineString(shell);
				asset.setRoute(route);
			}

			assetRepository.save(asset);


		} catch (JsonProcessingException e) {
			LOG.error(e);
			throw new IllegalArgumentException(e);
		}

	}

}
