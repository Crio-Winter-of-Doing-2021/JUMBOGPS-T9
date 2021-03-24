package com.crio.jumbotail.assettracking.others;

import static org.junit.jupiter.api.Assertions.assertFalse;


import com.bedatadriven.jackson.datatype.jts.JtsModule;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;

@Disabled
class BasicTests {

	@Test
	void given_timestamp_both_object_are_same() {

		long utc2_02_PM_14_mar_2021 = 1615730554;

		final Instant instant = Instant.ofEpochSecond(utc2_02_PM_14_mar_2021);
		final LocalDateTime localDateTimeFromInstant = LocalDateTime.ofInstant(instant, ZoneOffset.systemDefault());
		final LocalDateTime localDateTimeDirect = LocalDateTime.ofEpochSecond(utc2_02_PM_14_mar_2021, 0, ZoneOffset.UTC);


		assertFalse(localDateTimeDirect.isEqual(localDateTimeFromInstant));
	}

	@Test
	void some() {
		final Instant instant = Instant.now();
		final LocalDateTime localDateTimeInUtc = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);

		LocalDateTime now = LocalDateTime.now();

		System.out.println("now = " + now);
		System.out.println("localDateTimeInUtc = " + localDateTimeInUtc);

		assertFalse(now.isEqual(localDateTimeInUtc));

	}

	@Test
	void test_geo_fencing() {

		GeometryFactory gf = new GeometryFactory(new PrecisionModel(), 4326);

		final ArrayList<Coordinate> points = new ArrayList<>();
		points.add(new Coordinate(-10, -10));
		points.add(new Coordinate(-10, 10));
		points.add(new Coordinate(10, 10));
		points.add(new Coordinate(10, -10));
		points.add(new Coordinate(-10, -10));

		final Polygon polygon = gf.createPolygon(points.toArray(new Coordinate[0]));

		for (int i = 0; i < 20; i++) {
			final Coordinate coord = new Coordinate(i, i);
			final Point point = gf.createPoint(coord);
			System.out.println(point.within(polygon));
		}


	}

	@Test
	void test_Point_user_data() throws JsonProcessingException {
		GeometryFactory gf = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);
		final Point point = gf.createPoint(new Coordinate(80.911597, 26.828994));
		Map<String, String> map = new HashMap<>();
		map.put("key", "val");
		point.setUserData(map);


		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JtsModule(gf));

		System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(point));

	}


	@Test
	void try_faker() {
		Faker faker = new Faker(new Locale("en-IND"));
		for (int i = 0; i < 100; i++) {
			System.out.println(MessageFormat.format("longitude {0}, latitude {1}", faker.address().longitude(), faker.address().latitude()));
		}

	}


	@Test
	void marshalling_geojson() throws JsonProcessingException {
		String linestring = "{\n"
		              + "\t\t\t\t\"type\": \"LineString\",\n"
		              + "\t\t\t\t\"coordinates\": [\n"
		              + "\t\t\t\t\t[\n"
		              + "\t\t\t\t\t\t80.911444,\n"
		              + "\t\t\t\t\t\t26.828994\n"
		              + "\t\t\t\t\t],\n"
		              + "\t\t\t\t\t[\n"
		              + "\t\t\t\t\t\t80.916357,\n"
		              + "\t\t\t\t\t\t26.836392\n"
		              + "\t\t\t\t\t],\n"
		              + "\t\t\t\t\t[\n"
		              + "\t\t\t\t\t\t80.925416,\n"
		              + "\t\t\t\t\t\t26.841461\n"
		              + "\t\t\t\t\t],\n"
		              + "\t\t\t\t\t[\n"
		              + "\t\t\t\t\t\t80.94123,\n"
		              + "\t\t\t\t\t\t26.838173\n"
		              + "\t\t\t\t\t]\n"
		              + "\t\t\t\t]\n"
		              + "\t\t\t}";

		String polygon = "{\n"
		                 + "\t\t\t\t\"type\": \"Polygon\",\n"
		                 + "\t\t\t\t\"coordinates\": [\n"
		                 + "\t\t\t\t\t[\n"
		                 + "\t\t\t\t\t\t[\n"
		                 + "\t\t\t\t\t\t\t80.907176,\n"
		                 + "\t\t\t\t\t\t\t26.832709\n"
		                 + "\t\t\t\t\t\t],\n"
		                 + "\t\t\t\t\t\t[\n"
		                 + "\t\t\t\t\t\t\t80.934417,\n"
		                 + "\t\t\t\t\t\t\t26.851813\n"
		                 + "\t\t\t\t\t\t],\n"
		                 + "\t\t\t\t\t\t[\n"
		                 + "\t\t\t\t\t\t\t80.950529,\n"
		                 + "\t\t\t\t\t\t\t26.830439\n"
		                 + "\t\t\t\t\t\t],\n"
		                 + "\t\t\t\t\t\t[\n"
		                 + "\t\t\t\t\t\t\t80.918412,\n"
		                 + "\t\t\t\t\t\t\t26.810101\n"
		                 + "\t\t\t\t\t\t],\n"
		                 + "\t\t\t\t\t\t[\n"
		                 + "\t\t\t\t\t\t\t80.907176,\n"
		                 + "\t\t\t\t\t\t\t26.832709\n"
		                 + "\t\t\t\t\t\t]\n"
		                 + "\t\t\t\t\t]\n"
		                 + "\t\t\t\t]\n"
		                 + "\t\t\t}";

		GeometryFactory gf = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JtsModule(gf));

		Geometry line = mapper.readValue(linestring, Geometry.class);
		Geometry geofence = mapper.readValue(polygon, Geometry.class);

		System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(line));
		System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(geofence));


	}

}

