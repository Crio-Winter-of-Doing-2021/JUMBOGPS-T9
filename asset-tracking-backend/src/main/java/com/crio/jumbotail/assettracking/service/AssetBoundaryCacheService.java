package com.crio.jumbotail.assettracking.service;

import java.util.Optional;
import org.locationtech.jts.geom.Geometry;

public interface AssetBoundaryCacheService {

	Geometry put(String key, Geometry geometry);

	Optional<Geometry> get(String key);

}
