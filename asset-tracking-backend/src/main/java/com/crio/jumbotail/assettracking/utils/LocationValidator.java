package com.crio.jumbotail.assettracking.utils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;

public class LocationValidator implements ConstraintValidator<LocationConstraint, Point> {

	@Override
	public void initialize(LocationConstraint contactNumber) {
	}

	@Override
	public boolean isValid(Point p, ConstraintValidatorContext cxt) {

		final int numPoints = p.getNumPoints();
		final Coordinate coordinate = p.getCoordinate();
		final double longitude = coordinate.getX();
		final double latitude = coordinate.getY();
//	@Min(-180)
//	@Max(180)


		//	@Min(-180)
//	@Max(180)
		return (longitude >= -180 && longitude <= 180) &&
		       (latitude >= -90 && latitude <= 90) &&
		       numPoints == 1;
//        return contactField != null && contactField.matches("[0-9]+")
//          && (contactField.length() > 8) && (contactField.length() < 14);
//		return true;
	}

}