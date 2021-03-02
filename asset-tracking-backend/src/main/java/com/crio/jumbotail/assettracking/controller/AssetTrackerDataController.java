package com.crio.jumbotail.assettracking.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AssetTrackerDataController {

	@GetMapping("/markers")
	public void getMarkers(Pageable pageable) {

	}

}
