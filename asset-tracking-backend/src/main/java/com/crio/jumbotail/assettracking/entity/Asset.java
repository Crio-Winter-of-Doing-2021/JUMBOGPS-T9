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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;

@ToString
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@AllArgsConstructor
@Builder
@DynamicUpdate
public class Asset implements Serializable {

	@Id
	@SequenceGenerator(name = "asset_id_seq_gen", sequenceName = "asset_id_sequence", initialValue = 1000)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "asset_id_seq_gen")
	private Long id;

	private String title;
	private String description;
	private String assetType;
	private Polygon geofence;
	private LineString route;

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	@JsonManagedReference("asset-data")
	@OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, mappedBy = "asset", fetch = FetchType.LAZY)
	private List<LocationData> locationHistory;

	// extra properties
	private LocalDateTime lastReportedTimestamp;
	private Location lastReportedLocation;
	private Point lastReportedCoordinates;

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
		locationData.setAsset(this);
		if (locationHistory == null) {
			locationHistory = new ArrayList<>();
		}

		this.locationHistory.add(locationData);
	}

}
