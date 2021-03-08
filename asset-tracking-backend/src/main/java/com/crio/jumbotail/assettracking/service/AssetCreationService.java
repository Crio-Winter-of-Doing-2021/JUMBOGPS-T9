package com.crio.jumbotail.assettracking.service;

import com.crio.jumbotail.assettracking.entity.Asset;
import com.crio.jumbotail.assettracking.entity.Location;
import com.crio.jumbotail.assettracking.entity.LocationData;
import com.crio.jumbotail.assettracking.exceptions.AssetNotFoundException;
import com.crio.jumbotail.assettracking.exchanges.AssetCreatedResponse;
import com.crio.jumbotail.assettracking.exchanges.AssetCreationRequest;
import com.crio.jumbotail.assettracking.exchanges.LocationUpdateRequest;
import com.crio.jumbotail.assettracking.repositories.AssetRepository;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class AssetCreationService {

	@Autowired
	private AssetRepository assetRepository;

	@Autowired
	private ModelMapper modelMapper;

	public AssetCreatedResponse createAsset(AssetCreationRequest assetCreationRequest) {
		// TODO : validations ?

		LOG.info("assetCreationRequest [{}]", assetCreationRequest);

		final Location location = modelMapper.map(assetCreationRequest.getLocation().getLocationDto(), Location.class);
		LocationData locationData = new LocationData(location, assetCreationRequest.getLocation().getDeviceTimestamp());
		LOG.info("MODEL MAPPER location [{}]", location);
		final Asset asset = new Asset(
				assetCreationRequest.getTitle(),
				assetCreationRequest.getDescription(),
				assetCreationRequest.getAssetType(),
				locationData);


		asset.addLocationHistory(locationData);

		final Asset save = assetRepository.save(asset);

		final AssetCreatedResponse assetCreatedResponse = new AssetCreatedResponse();
		assetCreatedResponse.setId(save.getId());

		return assetCreatedResponse;
	}

	public void updateLocationDataForAsset(LocationUpdateRequest locationUpdateRequest, Long assetId) {

		final Optional<Asset> asset = assetRepository.findById(assetId);
		if (asset.isPresent()) {
			final Location location = modelMapper.map(locationUpdateRequest.getLocation().getLocationDto(), Location.class);
			LocationData locationData = new LocationData(location, locationUpdateRequest.getLocation().getDeviceTimestamp());
			asset.get().addLocationHistory(locationData);

		} else {
			throw new AssetNotFoundException("asset with " + assetId + " not found.");
		}

	}

}
