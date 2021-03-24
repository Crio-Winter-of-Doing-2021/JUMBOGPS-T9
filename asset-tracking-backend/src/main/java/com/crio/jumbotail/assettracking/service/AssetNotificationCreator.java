package com.crio.jumbotail.assettracking.service;

import com.crio.jumbotail.assettracking.entity.Location;
import org.locationtech.jts.geom.Geometry;

/**
 * Triggers a notification event if the asset does not confine to boundaries
 */
public interface AssetNotificationCreator {

	/**
	 *  @param assetId the id of the asset to validate
	 * @param location the latest location of the asset
	 * @param route the route the asset needs to follow
	 * @param geofence the geofence the asset needs to limit itself to
	 */
	void validateAssetLocation(Long assetId, Location location, Geometry route, Geometry geofence);

}
