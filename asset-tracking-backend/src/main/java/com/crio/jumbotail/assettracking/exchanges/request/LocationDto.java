package com.crio.jumbotail.assettracking.exchanges.request;

import java.io.Serializable;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class LocationDto implements Serializable {

	@NotNull
	@Min(-180)
	@Max(180)
	private Double longitude;

	@NotNull
	@Min(-180)
	@Max(180)
	private Double latitude;

}
