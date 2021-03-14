package com.crio.jumbotail.assettracking.spatial;

import com.crio.jumbotail.assettracking.entity.Location;
import java.util.List;
import org.locationtech.jts.algorithm.ConvexHull;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.stereotype.Component;

@Component
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

}
