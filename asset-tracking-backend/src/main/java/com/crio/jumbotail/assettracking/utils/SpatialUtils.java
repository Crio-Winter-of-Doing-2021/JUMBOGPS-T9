package com.crio.jumbotail.assettracking.utils;

import com.crio.jumbotail.assettracking.entity.Asset;
import com.crio.jumbotail.assettracking.entity.Location;
import com.crio.jumbotail.assettracking.exchanges.request.LocationDto;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.locationtech.jts.algorithm.ConvexHull;
import org.locationtech.jts.geom.Coordinate;
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

	private static final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);


	public static Location toLocation(Coordinate coordinate) {
		return new Location(coordinate.getOrdinate(LONGITUDE), coordinate.getOrdinate(LATITUDE));
	}

	public static Coordinate fromLocation(Location location) {
		return new Coordinate(location.getLongitude(), location.getLatitude());
	}

	public static Point pointFromLocation(Location location) {
		return geometryFactory.createPoint(new Coordinate(location.getLongitude(), location.getLatitude()));
	}


	public static Point fromCoordinate(Coordinate coordinate) {
		return geometryFactory.createPoint(coordinate);
	}

	public static Location getCentroid(List<Point> points) {

		final Coordinate[] coordinates = points.stream().map(Point::getCoordinate).toArray(Coordinate[]::new);

		final Point centroid = new ConvexHull(coordinates, new GeometryFactory()).getConvexHull().getCentroid();
		final Coordinate coordinate = centroid.getCoordinate();

		return toLocation(coordinate);
	}

	public static Location getCentroidForAssets(List<Asset> assets) {
		final List<Point> points = assets.stream().map(Asset::getLastReportedCoordinates).collect(Collectors.toList());

		final Location centroid = getCentroid(points);

		LOG.info("centroid [{}]", centroid);

		return centroid;
	}

	public static LocationDto addMetersToCurrent(Location location, double meters) {
		return addMetersToCurrent(location.getLatitude(), location.getLongitude(), meters);
	}

	public static LocationDto addMetersToCurrent(LocationDto location, double meters) {
		return addMetersToCurrent(location.getLatitude(), location.getLongitude(), meters);
	}

	public static LocationDto addMetersToCurrent(double currLatitude, double currLongitude, double meters) {

		// number of km per degree = ~111km (111.32 in google maps, but range varies
		// between 110.567km at the equator and 111.699km at the poles)
		// 1km in degree = 1 / 111.32km = 0.0089
		// 1m in degree = 0.0089 / 1000 = 0.0000089
		double coef = meters * 0.0000089;

		double newLatitude = currLatitude + coef;

		// pi / 180 = 0.018
		double newLongitude = currLongitude + coef / Math.cos(currLatitude * 0.018);

		return new LocationDto(newLongitude, newLatitude);
	}
}
