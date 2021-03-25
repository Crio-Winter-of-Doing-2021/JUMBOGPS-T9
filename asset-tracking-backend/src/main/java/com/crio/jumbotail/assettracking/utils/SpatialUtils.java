package com.crio.jumbotail.assettracking.utils;

import com.crio.jumbotail.assettracking.entity.Asset;
import com.crio.jumbotail.assettracking.entity.LocationData;
import com.crio.jumbotail.assettracking.exceptions.InvalidLocationException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import lombok.extern.log4j.Log4j2;
import org.locationtech.jts.algorithm.ConvexHull;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class SpatialUtils {
	// x - longitude
	public static final int LATITUDE = 1;
	// y - latitude
	public static final int LONGITUDE = 0;

	private static final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);

	private SpatialUtils() {
	}

	public static Point fromCoordinate(Coordinate coordinate) {
		return geometryFactory.createPoint(coordinate);
	}

	public static Point getCentroid(Coordinate[] coordinates) {
		final Point centroid = new ConvexHull(coordinates, new GeometryFactory()).getConvexHull().getCentroid();
		final Coordinate coordinate = centroid.getCoordinate();

		return geometryFactory.createPoint(coordinate);
	}

	public static Point getCentroidForAssets(List<Asset> assets) {
		final Coordinate[] coordinates = assets.stream()
				.map(Asset::getLastReportedCoordinates)
				.map(Point::getCoordinate).toArray(Coordinate[]::new);

		final Point centroid = getCentroid(coordinates);

		LOG.info("centroid [{}]", centroid);

		return centroid;
	}

	public static Point getCentroidForHistory(List<LocationData> locationData) {
		final Coordinate[] coordinates = locationData.stream()
				.map(LocationData::getCoordinates)
				.map(Point::getCoordinate)
				.toArray(Coordinate[]::new);

		final Point centroid = getCentroid(coordinates);

		LOG.info("centroid [{}]", centroid);

		return centroid;
	}

	public static Point addMetersToCurrent(Point location, double meters) {
		return addMetersToCurrent(location.getCoordinate().getY(), location.getCoordinate().getX(), meters);
	}

//	public static Point addMetersToCurrent(Point location, double meters) {
//		return addMetersToCurrent(location.getCoordinate().getY(), location.getCoordinate().getX(), meters);
//	}

	public static Point addMetersToCurrent(double currLatitude, double currLongitude, double meters) {

		// number of km per degree = ~111km (111.32 in google maps, but range varies
		// between 110.567km at the equator and 111.699km at the poles)
		// 1km in degree = 1 / 111.32km = 0.0089
		// 1m in degree = 0.0089 / 1000 = 0.0000089
		double coef = meters * 0.0000089;

		double newLatitude = currLatitude + coef;

		// pi / 180 = 0.018
		double newLongitude = currLongitude + coef / Math.cos(currLatitude * 0.018);

		return geometryFactory.createPoint(new Coordinate(newLongitude, newLatitude));
	}

	public static void validateGeometry(Geometry g) {

		if (g != null) {
			final Coordinate[] coordinates = g.getCoordinates();
			validateCoordinatesForEarthLatLong(coordinates);
		}

	}

	private static void validateCoordinatesForEarthLatLong(Coordinate[] coordinates) {
		LOG.debug(Arrays.toString(coordinates));
		for (Coordinate coordinate : coordinates) {
			final double longitude = coordinate.getX();
			final double latitude = coordinate.getY();

			if (!(longitude >= -180 && longitude <= 180 && latitude >= -180 && latitude <= 180)) {
				throw new InvalidLocationException(MessageFormat.format("Longitude {0} or Latitude {1} is invalid", longitude, latitude));
			}
		}
	}
}
