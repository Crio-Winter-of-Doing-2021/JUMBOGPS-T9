package com.crio.jumbotail.assettracking.exchanges;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LocationDto {

	@NotNull
	@Min(-180)
	@Max(180)
	private Double longitude;

	@NotNull
	@Min(-180)
	@Max(180)
	private Double latitude;

	public LocationDto(@NotNull @Min(-180) @Max(180) Double longitude, @NotNull @Min(-180) @Max(180) Double latitude) {
		this.longitude = longitude;
		this.latitude = latitude;
	}
}
