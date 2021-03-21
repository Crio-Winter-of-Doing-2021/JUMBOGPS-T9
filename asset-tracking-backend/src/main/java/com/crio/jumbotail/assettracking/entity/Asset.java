package com.crio.jumbotail.assettracking.entity;

import static com.crio.jumbotail.assettracking.utils.SpatialUtils.pointFromLocation;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.SequenceGenerator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Asset implements Serializable {

	@Id
	@SequenceGenerator(name = "tcode_data_seq_gen", sequenceName = "tcode_data_id_sequence", initialValue = 1000)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tcode_data_seq_gen")
	private Long id;

	private String title;
	private String description;
	private String assetType;

	private LocalDateTime lastReportedTimestamp;

	private Location lastReportedLocation;

	private Point lastReportedCoordinates;

	private Polygon geofence;
	private LineString route;

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	@JsonManagedReference("asset-data")
	@OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, mappedBy = "asset", fetch = FetchType.LAZY)
	private List<LocationData> locationHistory = new ArrayList<>();

	public Asset(String title, String description, String assetType, LocationData locationData) {
		this.title = title;
		this.description = description;
		this.assetType = assetType;

		locationData.setAsset(this);

		this.lastReportedLocation = locationData.getLocation();
		this.lastReportedTimestamp = locationData.getTimestamp();
		this.locationHistory.add(locationData);
	}

	// used when creating an asset

	@PrePersist
	@PreUpdate
	public void updateCoordinate() {
		if (this.getLastReportedLocation().getLatitude() == null || this.getLastReportedLocation().getLongitude() == null) {
			this.lastReportedCoordinates = null;
		} else {
			this.lastReportedCoordinates = pointFromLocation(this.lastReportedLocation);
		}
	}

	public void addLocationHistory(LocationData locationData) {

		// update location of asset to last location
		this.setLastReportedTimestamp(locationData.getTimestamp());
		this.setLastReportedLocation(locationData.getLocation());

		locationData.setAsset(this);

		this.locationHistory.add(locationData);
	}

	@Override
	public String toString() {
		return "Asset{"
		       + " id=" + id
		       + ", title='" + title + '\''
		       + ", description='" + description + '\''
		       + ", assetType='" + assetType + '\''
		       + ", lastReportedLocation=" + lastReportedLocation
		       + ", lastReportedTimestamp=" + lastReportedTimestamp
		       + '}';
	}

}
