package com.crio.jumbotail.assettracking.exchanges;

import java.util.Optional;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Location {

	@NotNull
	@Min(-180)
	@Max(180)
	double latitude;

	@NotNull
	@Min(-180)
	@Max(180)
	double longitude;

	/*
		timestamps -
				 should we create it on the backend or let the device transmit ?
		Scenario -
			 an asset sends an update but the network fails to deliver as asset is in transit and poor network conditions
			 asset keeps retrying and data transmits after say 2 hours
			 then the timestamp data is technically incorrect now
			 BUT
			 if asset timestamp is not generated at backend
			 a malicious device can transmit wrong timestamps
		*/
	private Optional<String> timestamp;


}
