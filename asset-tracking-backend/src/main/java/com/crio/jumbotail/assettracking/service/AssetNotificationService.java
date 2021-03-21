package com.crio.jumbotail.assettracking.service;

import com.crio.jumbotail.assettracking.entity.Location;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;

public interface AssetNotificationService {

	void validateAssetLocationForAnomaly(Long assetId, Location location, LineString route, Polygon geofence);

}
