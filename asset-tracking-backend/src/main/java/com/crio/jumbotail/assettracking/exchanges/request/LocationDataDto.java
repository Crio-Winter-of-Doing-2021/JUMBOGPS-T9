package com.crio.jumbotail.assettracking.exchanges.request;

import com.crio.jumbotail.assettracking.utils.LocationConstraint;
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LocationDataDto implements Serializable {

	@LocationConstraint
	@NotNull
	private Point coordinates;

	@NotNull
	private Long deviceTimestamp;

	public LocationDataDto(Point coordinates, long deviceTimestamp) {
		this.coordinates = coordinates;
		this.deviceTimestamp = deviceTimestamp;
	}

}
