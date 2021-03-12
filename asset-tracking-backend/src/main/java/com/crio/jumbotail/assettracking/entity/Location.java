package com.crio.jumbotail.assettracking.entity;

import java.io.Serializable;
import javax.persistence.Embeddable;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Location implements Serializable {

	@NotNull
	@Min(-180)
	@Max(180)
	private Double longitude;

	@NotNull
	@Min(-90)
	@Max(90)
	private Double latitude;


}
