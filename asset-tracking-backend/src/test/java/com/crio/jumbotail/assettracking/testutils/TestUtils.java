package com.crio.jumbotail.assettracking.testutils;

import com.bedatadriven.jackson.datatype.jts.JtsModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;


public class TestUtils {

	public static long asEpoch(LocalDateTime ldt) {
		return ldt.toEpochSecond(ZoneOffset.UTC);
	}

	public static final GeometryFactory gf = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);

	public static final ObjectMapper mapper = new ObjectMapper();

	static {
		mapper.registerModule(new JtsModule(gf));
	}

	public static String asJsonString(final Object obj) {
		try {
			return mapper.writeValueAsString(obj);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

//	int RADIUS_OF_EARTH = 6378 * 1000;

	//	void addToCurrentLocation(double latitude, double longitude, int metres) {
//
//		double newLatitude = latitude + ((double) metres / RADIUS_OF_EARTH) * (180 / Math.PI);
//		double newLongitude = longitude + +((double) metres / RADIUS_OF_EARTH) * ((180 / Math.PI) / Math.cos(latitude * Math.PI / 180));
//
//		LocationDto locationDto = new LocationDto(newLatitude, newLongitude);
//	}
//
////	public LocationDto addMetersToCurrent(Location location, double meters) {
////		return addMetersToCurrent(location.getLatitude(), location.getLongitude(), meters);
////	}
//
//	public LocationDto addMetersToCurrent(LocationDto location, double meters) {
//		return addMetersToCurrent(location.getLatitude(), location.getLongitude(), meters);
//	}
//
	public static Point addMetersToCurrent(Point point, double meters) {

		// number of km per degree = ~111km (111.32 in google maps, but range varies
		// between 110.567km at the equator and 111.699km at the poles)
		// 1km in degree = 1 / 111.32km = 0.0089
		// 1m in degree = 0.0089 / 1000 = 0.0000089
		double coef = meters * 0.0000089;

		double my_long = point.getX();
		double my_lat = point.getY();
		double new_lat = my_lat + coef;

		// pi / 180 = 0.018
		double new_long = my_long + coef / Math.cos(my_lat * 0.018);

		return gf.createPoint(new Coordinate(new_long, new_lat));
	}

}
