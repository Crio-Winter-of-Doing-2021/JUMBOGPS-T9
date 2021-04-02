package com.crio.jumbotail.assettracking.service;

import org.locationtech.jts.geom.Geometry;

public interface AssetBoundaryCacheService {

//	Geometry put(String key, Geometry geometry);

	Geometry get(String key);

}
