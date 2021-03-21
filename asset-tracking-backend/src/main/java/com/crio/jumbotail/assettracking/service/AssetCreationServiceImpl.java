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
	private AssetNotificationService notificationService;

	@Override
	public AssetCreatedResponse createAsset(AssetCreationRequest assetCreationRequest) {
		// TODO : validations ?

		LOG.debug("assetCreationRequest [{}]", assetCreationRequest);

		final Location location = modelMapper.map(assetCreationRequest.getLocation().getLocationDto(), Location.class);
		LOG.debug("MODEL MAPPER location [{}]", location);
		LocationData locationData = new LocationData(location, assetCreationRequest.getLocation().getDeviceTimestamp());
		final Asset asset = new Asset(
				assetCreationRequest.getTitle(),
				assetCreationRequest.getDescription(),
				assetCreationRequest.getAssetType(),
				locationData);


//		asset.addLocationHistory(locationData);

		final Asset save = assetRepository.save(asset);

		final AssetCreatedResponse assetCreatedResponse = new AssetCreatedResponse();
		assetCreatedResponse.setId(save.getId());

		return assetCreatedResponse;
	}

	@Override
	public void updateLocationDataForAsset(LocationUpdateRequest locationUpdateRequest, Long assetId) {
		// find the proxy asset
		// will throw an exception if not present
		try {
			final Asset asset = assetRepository.getOne(assetId);

			final Location location = modelMapper.map(locationUpdateRequest.getLocation().getLocationDto(), Location.class);
			final LocationData locationData = new LocationData(location, locationUpdateRequest.getLocation().getDeviceTimestamp());
			locationData.setAsset(asset);


			locationDataRepository.save(locationData);

			notificationService.validateAssetLocationForAnomaly(
					assetId,
					location,
					getRouteForAsset(assetId),
					getGeofenceForAsset(assetId)
			);

//			updateLocationDataBasedOnAssetRestrictions(assetId, asset, locationData);

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

//	private void updateLocationDataBasedOnAssetRestrictions(Long assetId, Asset asset, LocationData locationData) {
//		checkForGeofenceRestriction(assetId, asset, locationData);
//		checkForRouteRestriction(assetId, asset, locationData);
//	}
//
//	private void checkForGeofenceRestriction(Long assetId, Asset asset, LocationData locationData) {
//		Polygon geofence = assetGeofenceCache.getOrDefault(assetId, asset.getGeofence());
//		if (geofence != null) {
//			final Point point = SpatialUtils.pointFromLocation(locationData.getLocation());
//			locationData.setWithinGeofence(point.within(geofence));
//		}
//	}
//
//	private void checkForRouteRestriction(Long assetId, Asset asset, LocationData locationData) {
//		final LineString routeToFollow = assetRouteCache.getOrDefault(assetId, asset.getRoute());
//		if (routeToFollow != null) {
//			final Point point = SpatialUtils.pointFromLocation(locationData.getLocation());
//			locationData.setWithinGeofence(point.within(routeToFollow));
//		}
//	}

	@Override
	public void addBoundaryToAsset(Long assetId, String boundaryType, String data) {
		final ArrayList<Coordinate> points = new ArrayList<>();
		points.add(new Coordinate(-10, -10));
		points.add(new Coordinate(-10, 10));
		points.add(new Coordinate(10, 10));
		points.add(new Coordinate(10, -10));
		points.add(new Coordinate(-10, -10));

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

	}

}
