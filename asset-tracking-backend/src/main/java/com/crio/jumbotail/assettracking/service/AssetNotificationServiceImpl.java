package com.crio.jumbotail.assettracking.service;

import com.crio.jumbotail.assettracking.entity.Location;
import com.crio.jumbotail.assettracking.exchanges.Notification;
import com.crio.jumbotail.assettracking.utils.SpatialUtils;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class AssetNotificationServiceImpl implements AssetNotificationService {

	@Autowired
	public ApplicationEventPublisher eventPublisher;

	public void validateAssetLocationForAnomaly(Long assetId, Location location, LineString route, Polygon geofence) {

		final Point point = SpatialUtils.pointFromLocation(location);

		notifyForRouteDeviation(assetId, point, route);
		notifyForGeofenceDeviation(assetId, point, geofence);

	}

	private void notifyForRouteDeviation(Long assetId, Point point, LineString route) {
		if (route != null && !point.within(route)) {
			this.eventPublisher.publishEvent(new Notification(assetId,
					assetId + "is not following defined route",
					"route-deviation"
			));
		}
	}

	private void notifyForGeofenceDeviation(Long assetId, Point point, Polygon geofence) {
		if (geofence != null && !point.within(geofence)) {
			this.eventPublisher.publishEvent(new Notification(assetId,
					assetId + "is outside defined geofence",
					"geofence-exit"
			));
		}
	}


}
