package com.crio.jumbotail.assettracking.service;

import com.crio.jumbotail.assettracking.entity.Location;
import com.crio.jumbotail.assettracking.exchanges.response.Notification;
import com.crio.jumbotail.assettracking.utils.SpatialUtils;
import java.text.MessageFormat;
import lombok.extern.log4j.Log4j2;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class AssetNotificationCreatorImpl implements AssetNotificationCreator {

	@Autowired
	public ApplicationEventPublisher eventPublisher;

	private static final String ROUTE_DEVIATION = "route-deviation";
	private static final String GEO_FENCE_EXIT = "geofence-exit";

	public void validateAssetLocation(Long assetId, Location location, Geometry route, Geometry geofence) {

		final Point point = SpatialUtils.pointFromLocation(location);

		notifyForRouteDeviation(assetId, point, route);
		notifyForGeofenceDeviation(assetId, point, geofence);

	}

	private void notifyForRouteDeviation(Long assetId, Point point, Geometry route) {
		if (route != null && !point.within(route)) {
			this.eventPublisher.publishEvent(
					new Notification(
							assetId,
							MessageFormat.format("Asset {0} is not following defined route", String.valueOf(assetId)),
							ROUTE_DEVIATION
					));
			LOG.info("Route Deviation Notification For Asset {}", assetId);
		}
	}

	private void notifyForGeofenceDeviation(Long assetId, Point point, Geometry geofence) {

		if (geofence != null && !point.within(geofence)) {
			this.eventPublisher.publishEvent(
					new Notification(
							assetId,
							MessageFormat.format("Asset {0} is outside defined geofence", String.valueOf(assetId)),
							GEO_FENCE_EXIT
					));
			LOG.info("GeoFence Exit Notification For Asset {}", assetId);
		}
	}


}
