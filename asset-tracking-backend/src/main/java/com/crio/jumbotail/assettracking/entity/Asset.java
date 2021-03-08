package com.crio.jumbotail.assettracking.entity;

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
import javax.persistence.SequenceGenerator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

	private Location lastReportedLocation;

	private LocalDateTime lastReportedTimestamp;

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	@JsonManagedReference("asset-data")
	@OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, mappedBy = "asset", fetch = FetchType.LAZY)
	private List<LocationData> locationHistory = new ArrayList<>();

	// used when creating an asset

	public Asset(String title, String description, String assetType, LocationData locationData) {
		this.title = title;
		this.description = description;
		this.assetType = assetType;

		locationData.setAsset(this);

		this.lastReportedLocation = locationData.getLocation();
		this.lastReportedTimestamp = locationData.getTimestamp();
		this.locationHistory.add(locationData);
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