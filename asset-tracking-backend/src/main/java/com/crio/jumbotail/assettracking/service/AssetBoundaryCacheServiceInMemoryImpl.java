package com.crio.jumbotail.assettracking.service;

import com.crio.jumbotail.assettracking.repositories.AssetRepository;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile("inmem-cache")
@Service
public class AssetBoundaryCacheServiceInMemoryImpl implements AssetBoundaryCacheService {

	private final Map<String, Geometry> assetBoundaryCache = new ConcurrentHashMap<>();

	@Autowired
	private AssetRepository assetRepository;

	@Override
	public Geometry get(String key) {

		if (assetBoundaryCache.containsKey(key)) {
			return assetBoundaryCache.get(key);
		} else {
			Optional<Geometry> geometry = Optional.empty();

			final String[] keySplit = key.split("-");
			final Long assetId = Long.valueOf(keySplit[1]);
			if (keySplit[0].equalsIgnoreCase("route")) {
				geometry = assetRepository.getRouteForAsset(assetId);
			} else if (keySplit[0].equalsIgnoreCase("geofence")) {
				geometry = assetRepository.getGeofenceForAsset(assetId);
			}

			assetBoundaryCache.put(key, geometry.orElse(null));

			return geometry.orElse(null);
		}
	}

}
