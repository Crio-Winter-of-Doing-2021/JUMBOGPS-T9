package com.crio.jumbotail.assettracking.cache;

import com.crio.jumbotail.assettracking.repositories.AssetRepository;
import com.crio.jumbotail.assettracking.service.AssetBoundaryCacheService;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.locationtech.jts.geom.Geometry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Log4j2
@Primary
@Service
public class AssetBoundaryCacheServiceEhCacheImpl implements AssetBoundaryCacheService {

	@Autowired
	AssetRepository assetRepository;

	@Cacheable(value = "assetBoundariesCache", key = "#key")
	@Override
	public Geometry get(String key) {
		LOG.info("EH CACHE TRIGGERED");
		Optional<Geometry> geometry = Optional.empty();

		final String[] keySplit = key.split("-");
		final Long assetId = Long.valueOf(keySplit[1]);
		if (keySplit[0].equalsIgnoreCase("route")) {
			geometry = assetRepository.getRouteForAsset(assetId);
		} else if (keySplit[0].equalsIgnoreCase("geofence")) {
			geometry = assetRepository.getGeofenceForAsset(assetId);
		}

		return geometry.orElse(null);
	}
}
