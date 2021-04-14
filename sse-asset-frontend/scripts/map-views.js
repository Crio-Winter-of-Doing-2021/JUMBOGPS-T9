// add all the layers
// keep them hidden
// show data for assetView Layer
//     call set data on the asset-data source
//     call showLayer
// show data for timelineView Layer
//     hide assetView Layer
//     call set data on the timeline-data source
//     call showLayer

const token = localStorage.getItem("token");

function getAssetDataAndRenderOnMap() {
  fetch("https://jumbogps-main.anugrahsinghal.repl.co/assets?limit=100", {
    method: "GET", // *GET, POST, PUT, DELETE, etc.
    headers: {
      Authorization:
        `Bearer ${token}`,
    },
  })
    .then((response) => {
      if (!response.ok) {
        throw Error(response.statusText);
      }
      response.json();
    })
    .then((data) => {
      showAssetView(data);
    })
    .catch((err) => {
      console.log(err);
      alert(err.message);
    });
}

function getTimelineDataAndRenderOnMap(assetId) {
  // call API using assetId

  showTimeLineView(historyDataFromAPIExtended);
}

function convertFromAssetResponseToGeoJson(data) {
  let assets = data.assets;
  let features = [];
  for (let i = 0; i < assets.length; i++) {
    features.push({
      type: "Feature",
      properties: {
        id: assets[i].id,
        title: assets[i].title,
        description: assets[i].description,
        assetType: assets[i].assetType,
        timestamp: assets[i].lastReportedTimestamp,
      },
      geometry: assets[i].lastReportedCoordinates,
    });
  }
  return {
    type: "FeatureCollection",
    features: features,
  };
}

function convertHistoryResponseToGeoJson(data) {
  let features = [];
  let geofence = data.asset.geofence;
  if (geofence !== undefined && geofence.type === "Polygon") {
    features.push({
      type: "Feature",
      properties: {},
      geometry: geofence,
    });
  }

  let route = data.asset.route;
  if (
    route !== undefined &&
    (route.type === "LineString" || route.type === "Polygon")
  ) {
    features.push({
      type: "Feature",
      properties: {},
      geometry: route,
    });
  }

  let timeLineData = data.history;
  let asset = data.asset;

  for (let i = 0; i < timeLineData.length; i++) {
    const element = timeLineData[i];

    features.push({
      type: "Feature",
      properties: {
        id: asset.id,
        title: asset.title,
        description: asset.description,
        assetType: asset.assetType,
        timestamp: element.timestamp,
        viewType: "timeline",
      },
      geometry: element.coordinates,
    });
  }

  return {
    type: "FeatureCollection",
    features: features,
  };
}

function showAssetView(data) {
  showOrHideLayers(timelineViewLayers,"visible","none");
  showOrHideLayers(heatmapLayers,"visible","none");

  if (data.assets.length === 0) {
    console.log("No Assets Found");
    triggerIframe("No Assets Found");
    return;
  }

  let geoJsonData = convertFromAssetResponseToGeoJson(data);

  map.getSource("asset-tracking-data").setData(geoJsonData);

  adjustMap(geoJsonData);

  showOrHideLayers(assetViewLayers,"none","visible");

  showPopupNotification(`${data.assets.length} assets found`);
}
// for showing 24 hrs history of an asset
function showTimeLineView(data) {
  showOrHideLayers(assetViewLayers,"visible","none");
  showOrHideLayers(heatmapLayers,"visible","none");

  if (data.history.length === 0) {
    console.log("No History For Asset in the last 24 hours");
    showPopupNotification("No History For That Asset present");
    return;
  }

  let geoJsonData = convertHistoryResponseToGeoJson(data);

  map.getSource("timeline-view").setData(geoJsonData);
  map
    .getSource("route-line-string")
    .setData(makeLineStringForGeoJsonTimelineView(geoJsonData));

  adjustMap(geoJsonData);

  showOrHideLayers(timelineViewLayers,"none","visible");
}

function adjustMap(geoJsonData) {
  map.fitBounds(turf.bbox(geoJsonData), { padding: 40, maxZoom: 12 });
}

function addImages(map, images) {
  const addImage = (map, id, url) => {
    return new Promise((resolve, reject) => {
      map.loadImage(url, (error, image) => {
        if (error) {
          reject(error);
          return;
        }
        map.addImage(id, image);
        resolve(image);
      });
    });
  };
  const promises = images.map((imageData) =>
    addImage(map, imageData.id, imageData.url)
  );
  return Promise.all(promises);
}

// for showing/hiding layers for different views
function showOrHideLayers(layerNames,prevState,currState) {
  if (layerNames !== undefined && layerNames.length > 0) {
    for (let i = 0; i < layerNames.length; i++) {
      let clickedLayer = layerNames[i];
      let visibility = map.getLayoutProperty(clickedLayer, "visibility");

      // Toggle layer visibility by changing the layout object's visibility property.
      if (visibility !== undefined && visibility === prevState) {
        map.setLayoutProperty(clickedLayer, "visibility", currState);
      }
    }
  }
}

// for showing route path of an asset
function makeLineStringForGeoJsonTimelineView(timelineViewGeoJsonData) {
  let features = timelineViewGeoJsonData.features;
  let lineStringCoordinates = [];
  for (let i = 0; i < features.length; i++) {
    if (features[i].geometry.type === "Point") {
      lineStringCoordinates.push(features[i].geometry.coordinates);
    }
  }
  let lineString = {
    type: "Feature",
    properties: {},
    geometry: {
      type: "LineString",
      coordinates: [lineStringCoordinates],
    },
  };
  return lineString;
}
