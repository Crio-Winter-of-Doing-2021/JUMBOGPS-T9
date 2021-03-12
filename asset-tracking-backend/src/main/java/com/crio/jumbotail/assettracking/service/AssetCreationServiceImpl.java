package com.crio.jumbotail.assettracking.service;

import com.crio.jumbotail.assettracking.entity.Asset;
import com.crio.jumbotail.assettracking.entity.Location;
import com.crio.jumbotail.assettracking.entity.LocationData;
import com.crio.jumbotail.assettracking.exceptions.AssetNotFoundException;
import com.crio.jumbotail.assettracking.exchanges.AssetCreatedResponse;
import com.crio.jumbotail.assettracking.exchanges.AssetCreationRequest;
import com.crio.jumbotail.assettracking.exchanges.LocationUpdateRequest;
import com.crio.jumbotail.assettracking.repositories.AssetRepository;
import com.crio.jumbotail.assettracking.repositories.LocationDataRepository;
import javax.persistence.EntityNotFoundException;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class AssetCreationServiceImpl implements AssetCreationService {

	@Autowired
	private AssetRepository assetRepository;

	@Autowired
	LocationDataRepository locationDataRepository;

	@Autowired
	private ModelMapper modelMapper;

	@Override
	public AssetCreatedResponse createAsset(AssetCreationRequest assetCreationRequest) {
		// TODO : validations ?

		LOG.debug("assetCreationRequest [{}]", assetCreationRequest);

		final Location location = modelMapper.map(assetCreationRequest.getLocation().getLocationDto(), Location.class);
		LOG.debug("MODEL MAPPER location [{}]", location);
		LocationData locationData = new LocationData(location, assetCreationRequest.getLocation().getDeviceTimestamp());
		final Asset asset = new Asset(
				assetCreationRequest.getTitle(),
				assetCreationRequest.getDescription(),
				assetCreationRequest.getAssetType(),
				locationData);


//		asset.addLocationHistory(locationData);

		final Asset save = assetRepository.save(asset);

		final AssetCreatedResponse assetCreatedResponse = new AssetCreatedResponse();
		assetCreatedResponse.setId(save.getId());

		return assetCreatedResponse;
	}

	@Override
	public boolean updateLocationDataForAsset(LocationUpdateRequest locationUpdateRequest, Long assetId) {

		boolean updated = false;

		// find the proxy asset
		// will throw an exception if not present
		try {
			final Asset asset = assetRepository.getOne(assetId);

			final LocationData locationData = new LocationData(
					new Location(locationUpdateRequest.getLocation().getLocationDto().getLatitude(),
							locationUpdateRequest.getLocation().getLocationDto().getLongitude()),
					locationUpdateRequest.getLocation().getDeviceTimestamp()
			);
			locationData.setAsset(asset);

			locationDataRepository.save(locationData);

			updated = true;

		} catch (EntityNotFoundException e) {
			throw new AssetNotFoundException("Asset with " + assetId + " not found.");
		}

		return updated;
	}

}
