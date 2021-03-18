package com.crio.jumbotail.assettracking.controller;

import static com.crio.jumbotail.assettracking.utils.SpatialUtils.addMetersToCurrent;
import static java.time.temporal.ChronoUnit.HOURS;


import com.crio.jumbotail.assettracking.entity.Asset;
import com.crio.jumbotail.assettracking.entity.Location;
import com.crio.jumbotail.assettracking.exchanges.AssetCreatedResponse;
import com.crio.jumbotail.assettracking.exchanges.AssetCreationRequest;
import com.crio.jumbotail.assettracking.exchanges.LocationDataDto;
import com.crio.jumbotail.assettracking.exchanges.LocationDto;
import com.crio.jumbotail.assettracking.exchanges.LocationUpdateRequest;
import com.crio.jumbotail.assettracking.repositories.AssetRepository;
import com.crio.jumbotail.assettracking.service.AssetCreationService;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Log4j2
@RestController
@RequestMapping("/create")
public class DataCreationController {

	@Value("classpath:locations.csv")
	Resource resourceFile;

	private static final ZoneOffset offset = OffsetDateTime.now().getOffset();

	@Autowired
	AssetCreationService assetCreationService;

	@Autowired
	AssetRepository assetRepository;

	private List<Long> mockData = new ArrayList<>();

	/**
	 * @param assetCreationRequest request to create a new asset with its initial location
	 * @return id of the created asset
	 */
	@PostMapping("/assets")
	@ResponseStatus(HttpStatus.CREATED)
	public AssetCreatedResponse createNewAsset(@RequestBody AssetCreationRequest assetCreationRequest) {
		return assetCreationService.createAsset(assetCreationRequest);
	}

	@PatchMapping("/assets/{assetId}")
	@ResponseStatus(HttpStatus.OK)
	public void updateLocationOfAsset(@RequestBody LocationUpdateRequest locationUpdateRequest, @PathVariable Long assetId) {
		assetCreationService.updateLocationDataForAsset(locationUpdateRequest, assetId);
	}

	@GetMapping("/mock-data")
	public void createData() throws IOException {
		final InputStream inputStream = resourceFile.getInputStream();
		String data = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());

		final String[] locations = data.split("\n");

		for (String location : locations) {
			final String[] s = location.split("\t");
			LOG.info("location = " + Arrays.toString(s));
			assert (s.length == 2);
			LocationDto locationDto = new LocationDto(Double.valueOf(s[0]), Double.valueOf(s[1]));

			LocationDataDto locationDataDto = new LocationDataDto(locationDto,
					Instant.now().plus((long) (Math.random() * 15000), ChronoUnit.SECONDS).getEpochSecond());

			AssetCreationRequest assetCreationRequest = new AssetCreationRequest(
					RandomStringUtils.randomAlphabetic(10),
					RandomStringUtils.randomAlphabetic(40),
					locationDataDto, RandomUtils.nextInt() % 2 == 0 ? "TRUCK" : "SALESPERSON");

			final AssetCreatedResponse newAsset = createNewAsset(assetCreationRequest);

			mockData.add(newAsset.getId());
		}

	}

	@GetMapping("/mock-history")
	public List<Long> createHistoryForAssets(@RequestParam int n) {

		for (Long mockAssetId : mockData.subList(0, n)) {
			final Optional<Asset> assetsFirstLocation = assetRepository.findById(mockAssetId);
			if (assetsFirstLocation.isPresent()) {
				final Location firstLocation = assetsFirstLocation.get().getLastReportedLocation();
				final LocalDateTime firstTimestamp = assetsFirstLocation.get().getLastReportedTimestamp();

				makeHistoryStartingBeforeNHours(mockAssetId, firstLocation, firstTimestamp, 36);
			} else {
				LOG.info("Mocked assets missing from DB - id {}", mockAssetId);
			}
		}

		return mockData.subList(0, n);
	}

	private void makeHistoryStartingBeforeNHours(Long mockAssetId, Location firstLocation, LocalDateTime firstTimestamp, int hoursBefore) {
		LocationDto locationDto = new LocationDto(firstLocation.getLongitude(), firstLocation.getLatitude());
		Instant timestampOfNHourBefore = firstTimestamp.toInstant(offset).minus(hoursBefore, HOURS);
		for (int i = 0; i < hoursBefore; i++) {

			locationDto = addMetersToCurrent(locationDto, 5000);
			LocationDataDto locationDataDto = new LocationDataDto(locationDto, timestampOfNHourBefore.getEpochSecond());

			updateLocationOfAsset(new LocationUpdateRequest(mockAssetId, locationDataDto), mockAssetId);

			// add an hour
			timestampOfNHourBefore = timestampOfNHourBefore.plus(1, HOURS);
		}
	}


}
