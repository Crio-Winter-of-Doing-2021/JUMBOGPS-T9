package com.crio.jumbotail.assettracking.entity;

import static java.time.ZoneId.systemDefault;


import com.crio.jumbotail.assettracking.utils.LocationConstraint;
import com.fasterxml.jackson.annotation.JsonBackReference;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.locationtech.jts.geom.Point;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class LocationData implements Serializable {

	@Id
	@SequenceGenerator(name = "location_data_id_seq_gen", sequenceName = "location_data_id_sequence")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "location_data_id_seq_gen")
	private Long id;

	//	private Location location;
	@NotNull
	private LocalDateTime timestamp;

	@LocationConstraint
	@NotNull
	private Point coordinates;

	public LocationData(Point coordinates, LocalDateTime timestamp) {
		this.coordinates = coordinates;
		this.timestamp = timestamp;
	}
//	public LocationData(Location location, LocalDateTime timestamp) {
//		this.location = location;
//		this.timestamp = timestamp;
//	}
//
//	public LocationData(Location location, Long timestampEpoch) {
//		this.location = location;
//		// convert epoch timestamp to LocalDateTime object
//		this.timestamp = LocalDateTime.ofInstant(Instant.ofEpochSecond(timestampEpoch), systemDefault());
//	}

	public LocationData(Point coordinates, Long timestampEpoch) {
		this.coordinates = coordinates;
		// convert epoch timestamp to LocalDateTime object
		this.timestamp = LocalDateTime.ofInstant(Instant.ofEpochSecond(timestampEpoch), systemDefault());
	}

	@JsonBackReference("asset-data")
	@ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY, optional = false)
	private Asset asset;

//
//	/**
//	 * updates the properties for the asset mapped to it and stores location data as Point[Geometry]
//	 */
//	@PrePersist
//	@PreUpdate
//	public void updateCoordinateAndAssetProperties() {
//		if (this.getLocation().getLongitude() == null || this.getLocation().getLatitude() == null) {
//			this.coordinates = null;
//		} else {
//			this.coordinates = pointFromLocation(this.location);
//			this.asset.setLastReportedLocation(this.location);
//			this.asset.setLastReportedTimestamp(this.timestamp);
//		}
//	}


	/**
	 * updates the properties for the asset mapped to it and stores location data as Point[Geometry]
	 * this way manually updating the owner Asset when a location is updated
	 */
	@PrePersist
	@PreUpdate
	public void updateAsset() {
		this.asset.setLastReportedCoordinates(this.coordinates);
		this.asset.setLastReportedTimestamp(this.timestamp);
	}


	public String toString() {
		return "LocationData(id=" + this.getId() + ", timestamp=" + this.getTimestamp() + ", coordinates=" + this.getCoordinates().getCoordinate() + ")";
	}

}
