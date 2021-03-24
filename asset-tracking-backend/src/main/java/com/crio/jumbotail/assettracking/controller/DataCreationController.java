package com.crio.jumbotail.assettracking.controller;

import static com.crio.jumbotail.assettracking.utils.SpatialUtils.addMetersToCurrent;
import static java.time.temporal.ChronoUnit.HOURS;


import com.crio.jumbotail.assettracking.entity.Asset;
import com.crio.jumbotail.assettracking.entity.Location;
import com.crio.jumbotail.assettracking.exchanges.request.AssetCreationRequest;
import com.crio.jumbotail.assettracking.exchanges.request.LocationDataDto;
import com.crio.jumbotail.assettracking.exchanges.request.LocationDto;
import com.crio.jumbotail.assettracking.exchanges.request.LocationUpdateRequest;
import com.crio.jumbotail.assettracking.exchanges.response.AssetCreatedResponse;
import com.crio.jumbotail.assettracking.exchanges.response.Subscriber;
import com.crio.jumbotail.assettracking.repositories.AssetRepository;
import com.crio.jumbotail.assettracking.service.AssetCreationService;
import com.crio.jumbotail.assettracking.service.SubscriptionService;
import com.github.javafaker.Faker;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import java.util.Locale;
import java.util.Optional;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
@Tag(name = "Asset Data Creator", description = "The Asset Data Creator API")
@RequestMapping("/api")
public class DataCreationController {

	@Autowired
	private SubscriptionService subscriptionService;

	@Operation(description = "Subscribe to events when an asset crosses the geofence/defined path",
			summary = "Subscribe to events"
	)
	@GetMapping(value = "/assets/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Subscriber subscribeToSSE(HttpServletResponse response) {
		response.setHeader("Cache-Control", "no-store");

		final Subscriber subscriber = new Subscriber();

		return subscriptionService.addSubscriber(subscriber);
	}

	//region hidden-content
	private static final ZoneOffset offset = OffsetDateTime.now().getOffset();

	@Value("classpath:locations.csv")
	Resource resourceFile;
	@Autowired
	AssetCreationService assetCreationService;

	@Autowired
	AssetRepository assetRepository;


	/**
	 * @param assetCreationRequest request to create a new asset with its initial location
	 * @return id of the created asset
	 */
	@Operation(description = "Create a new Asset",
			summary = "Create Asset"
	)
	@PostMapping("/assets")
	@ResponseStatus(HttpStatus.CREATED)
	public AssetCreatedResponse createNewAsset(@RequestBody AssetCreationRequest assetCreationRequest) {
		return assetCreationService.createAsset(assetCreationRequest);
	}

	/**
	 * @param assetId      id of the asset
	 * @param boundaryType Polygon or Linestring
	 * @param data         array of longitudes and latitudes
	 */
	@PostMapping("/assets/{assetId}/{boundaryType}")
	@ResponseStatus(HttpStatus.CREATED)
	public void createGeoFence(@PathVariable Long assetId, @PathVariable String boundaryType, @RequestBody String data) {
		assetCreationService.addBoundaryToAsset(assetId, boundaryType, data);
	}

	/**
	 * @param locationUpdateRequest body containing the updated data
	 * @param assetId               id of the asset
	 */

	@Operation(description = "Update the Location of asset of provided id",
			summary = "Update the Location of asset"
	)
	@PatchMapping("/assets/{assetId}")
	@ResponseStatus(HttpStatus.OK)
	public void updateLocationOfAsset(@RequestBody LocationUpdateRequest locationUpdateRequest, @PathVariable Long assetId) {
		assetCreationService.updateLocationDataForAsset(locationUpdateRequest, assetId);
	}

	private List<Long> mockData = new ArrayList<>();


	@Operation(summary = "Create MOCK DATA In DB")
	@GetMapping("/mock-data")
	public void createData(@RequestParam String global) throws IOException {

		Faker faker = new Faker(new Locale("en-IND"));

		if (global != null) {

			for (int i = 0; i < 100; i++) {
				LocationDto locationDto = new LocationDto(Double.valueOf(faker.address().longitude()), Double.valueOf(faker.address().latitude()));

				makeMockData(faker, locationDto);
			}

		} else {
			final InputStream inputStream = resourceFile.getInputStream();
			String data = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());

			final String[] locations = data.split("\n");

			for (String location : locations) {
				final String[] s = location.split("\t");
				LOG.info("location = " + Arrays.toString(s));
				assert (s.length == 2);
				LocationDto locationDto = new LocationDto(Double.valueOf(s[0]), Double.valueOf(s[1]));

				makeMockData(faker, locationDto);
			}
		}
	}

	private void makeMockData(Faker faker, LocationDto locationDto) {
		LocationDataDto locationDataDto = new LocationDataDto(locationDto,
				Instant.now().plus((long) (Math.random() * 15000), ChronoUnit.SECONDS).getEpochSecond());

		AssetCreationRequest assetCreationRequest = new AssetCreationRequest(
				faker.commerce().material(),
				faker.commerce().productName(),
				locationDataDto, RandomUtils.nextInt() % 2 == 0 ? "TRUCK" : "SALESPERSON");

		final AssetCreatedResponse newAsset = createNewAsset(assetCreationRequest);

		mockData.add(newAsset.getId());
	}

	@Operation(summary = "Create History for [N] MOCK DATA In DB",
			description = "Create 36 hour History for [N] MOCK DATA In DB, returns id for whom mock data was created")
	@GetMapping("/mock-history")
	public List<Long> createHistoryForAssets(@RequestParam int n) {

		for (Long mockAssetId : mockData.subList(0, n)) {
			final Optional<Asset> assetsFirstLocation = assetRepository.findById(mockAssetId);
			if (assetsFirstLocation.isPresent()) {
				// TODO
				// FIXME
				final Location firstLocation = null;//assetsFirstLocation.get().getLastReportedLocation();
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
	//endregion


}
