package com.crio.jumbotail.assettracking.entity;

import static com.crio.jumbotail.assettracking.utils.SpatialUtils.pointFromLocation;
import static java.time.ZoneId.systemDefault;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.locationtech.jts.geom.Point;

@Getter
@Setter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class LocationData implements Serializable {

	@Id
	@SequenceGenerator(name = "location_data_id_seq_gen", sequenceName = "location_data_id_sequence")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "location_data_id_seq_gen")
	private Long id;

	private Location location;
	private LocalDateTime timestamp;

	@JsonIgnore
	@Getter
	private Point coordinates;

	public LocationData(Location location, LocalDateTime timestamp) {
		this.location = location;
		this.timestamp = timestamp;
	}

	public LocationData(Location location, Long timestampEpoch) {
		this.location = location;
		this.timestamp = LocalDateTime.ofInstant(Instant.ofEpochSecond(timestampEpoch), systemDefault());
	}

	@JsonBackReference("asset-data")
	@ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY, optional = false)
	private Asset asset;


	@PrePersist
	@PreUpdate
	public void updateCoordinate() {
		if (this.getLocation().getLongitude() == null || this.getLocation().getLatitude() == null) {
			this.coordinates = null;
		} else {
			this.coordinates = pointFromLocation(this.location);
			this.asset.setLastReportedLocation(this.location);
			this.asset.setLastReportedTimestamp(this.timestamp);
		}
	}
}
