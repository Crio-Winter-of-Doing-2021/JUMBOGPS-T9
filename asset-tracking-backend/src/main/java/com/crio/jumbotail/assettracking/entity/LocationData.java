package com.crio.jumbotail.assettracking.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class LocationData implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	@SequenceGenerator(name = "location_id_generator")
	private Long id;

	private Location location;

	private LocalDateTime timestamp;

	public LocationData(Location location, LocalDateTime timestamp) {
		this.location = location;
		this.timestamp = timestamp;
	}

	public LocationData(Location location, Long timestampEpoch) {
		this.location = location;
		this.timestamp = LocalDateTime.ofEpochSecond(timestampEpoch, 0, ZoneOffset.UTC);
	}

	@JsonBackReference("asset-data")
	@ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY, optional = false)
	private Asset asset;

}
