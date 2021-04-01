package com.crio.jumbotail.assettracking.entity;

import com.crio.jumbotail.assettracking.exchanges.response.AssetExportData;
import com.crio.jumbotail.assettracking.utils.LocationConstraint;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedNativeQuery;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.SqlResultSetMappings;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;

@ToString
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@AllArgsConstructor
@Builder
@DynamicUpdate
@SqlResultSetMappings(value = {
		@SqlResultSetMapping(name = "ExportAssetsResult",
				classes = {
						@ConstructorResult(
								targetClass = AssetExportData.class,
								columns = {
								@ColumnResult(name = "assetId", type = Long.class),
								@ColumnResult(name = "assetType", type = String.class),
								@ColumnResult(name = "startTimestamp", type = LocalDateTime.class),
								@ColumnResult(name = "endTimestamp", type = LocalDateTime.class),
								@ColumnResult(name = "startLocation", type = Geometry.class),
								@ColumnResult(name = "endLocation", type = Geometry.class),
								@ColumnResult(name = "route", type = Geometry.class),
								@ColumnResult(name = "geofence", type = Geometry.class),
						})
				})
})
@NamedNativeQuery(
		name = "ExportAssets",
		query = "SELECT "
		        + "a.id as assetId,\n"
		        + "a.asset_type as assetType,\n"
		        + "l.timestamp as startTimestamp,\n"
		        + "a.last_reported_timestamp as endTimestamp,\n"
		        + "l.coordinates as startLocation,\n"
		        + "a.last_reported_coordinates as endLocation,\n"
		        + "a.route,\n"
		        + "a.geofence\n"
		        + "FROM asset a, location_data l\n"
		        + "WHERE l.id = (SELECT l.id FROM location_data l WHERE l.asset_id = a.id order by l.timestamp limit 1)",
		resultSetMapping = "ExportAssetsResult"
)
public class Asset implements Serializable {

	@Id
	@SequenceGenerator(name = "asset_id_seq_gen", sequenceName = "asset_id_sequence", initialValue = 1000)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "asset_id_seq_gen")
	private Long id;

	//	@Column(nullable = false)
	private String title;
	//	@Column(nullable = false)
	private String description;
	@Column(nullable = false, updatable = false)
	private String assetType;

	private Geometry geofence;
	private Geometry route;

	@JsonInclude(JsonInclude.Include.NON_EMPTY)
	@JsonManagedReference("asset-data")
	@OneToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, mappedBy = "asset", fetch = FetchType.LAZY)
	private List<LocationData> locationHistory;

	// extra properties - depend on LocationData to set value to these properties
	private LocalDateTime lastReportedTimestamp;
	@LocationConstraint
	private Point lastReportedCoordinates;

	public void addLocationHistory(LocationData locationData) {
		locationData.setAsset(this);
		if (locationHistory == null) {
			locationHistory = new ArrayList<>();
		}

		this.locationHistory.add(locationData);
	}

}
