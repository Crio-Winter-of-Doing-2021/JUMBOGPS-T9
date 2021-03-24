package com.crio.jumbotail.assettracking.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.locationtech.jts.geom.Geometry;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Primary
@Service
public class AssetBoundaryCacheServiceInMemoryImpl implements AssetBoundaryCacheService {

	private final Map<String, Geometry> assetBoundaryCache = new ConcurrentHashMap<>();

	@Override
	public Geometry put(String key, Geometry geometry) {
		return assetBoundaryCache.put(key, geometry);
	}

	@Override
	public Geometry get(String key) {
		return assetBoundaryCache.get(key);
	}

}
