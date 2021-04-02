package com.crio.jumbotail.assettracking.service;

import java.util.Optional;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

/**
 * Triggers a notification event if the asset does not confine to boundaries
 */
public interface AssetNotificationCreator {

	/**
	 * @param assetId  the id of the asset to validate
	 * @param location the latest location of the asset
	 * @param route    the route the asset needs to follow
	 * @param geofence the geofence the asset needs to limit itself to
	 */
	void validateAssetLocation(Long assetId, Point point, Optional<Geometry> route, Optional<Geometry> geofence);

}
