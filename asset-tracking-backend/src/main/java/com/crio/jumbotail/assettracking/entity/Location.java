package com.crio.jumbotail.assettracking.entity;

import java.io.Serializable;
import javax.persistence.Embeddable;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Embeddable
@Data
public class Location implements Serializable {

	@NotNull
	@Min(-90)
	@Max(90)
	private Double latitude;

	@NotNull
	@Min(-180)
	@Max(180)
	private Double longitude;

}
