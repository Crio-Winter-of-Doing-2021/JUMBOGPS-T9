mapboxgl.accessToken = mapboxAccessToken;

// configuring blank map
let map = new mapboxgl.Map({
  container: "map",
  style: "mapbox://styles/mapbox/streets-v11",
  center: [78.476681027237, 22.1991660760527],
  zoom: 3,
});

/* given a query in the form "lng, lat" or "lat, lng" returns the matching
 * geographic coordinate(s) as search results in carmen geojson format,
 * https://github.com/mapbox/carmen/blob/master/carmen-geojson.md
 */
const geocoder = new MapboxGeocoder({
  accessToken: mapboxgl.accessToken,
  mapboxgl: mapboxgl,
  localGeocoder: coordinatesGeocoder,
  marker: false,
});

map.addControl(geocoder, "top-left");

map.addControl(new mapboxgl.NavigationControl(), "bottom-right");

// initial load of map
map.on("load", function () {
  addImages(map, [
    { url: "icons/truck.png", id: "truck" },
    { url: "icons/person.png", id: "salesperson" },
    { url: "icons/truck.png", id: "truck+salesperson" },
    { url: "icons/custom_marker.png", id: "custom-marker" },
  ])
    .then(() => {
      addSourceAndLayersForAssetView();
      addSourceAndLayersForTimelineView();
      addLayerForHeatmapView();

      // inspect a cluster on click
      map.on("click", "clusters", function (e) {
        let features = map.queryRenderedFeatures(e.point, {
          layers: ["clusters"],
        });
        let clusterId = features[0].properties.cluster_id;
        map
          .getSource("asset-tracking-data")
          .getClusterExpansionZoom(clusterId, function (err, zoom) {
            if (err) return;

            map.easeTo({
              center: features[0].geometry.coordinates,
              zoom: zoom,
            });
          });
      });

      map.on("mouseenter", "clusters", function () {
        map.getCanvas().style.cursor = "pointer";
      });
      map.on("mouseleave", "clusters", function () {
        map.getCanvas().style.cursor = "";
      });

      let popup = new mapboxgl.Popup({});

      // When a click event occurs on a feature in
      // the unclustered-point layer, open a popup at
      // the location of the feature, with
      // description HTML from its properties.

      map.on("click", "unclustered-point", showPopup);
      map.on("click", "timeline-view-point", showPopup);

      map.on("mouseenter", "unclustered-point", showPopup);
      map.on("mouseenter", "timeline-view-point", showPopup);

      map.on("mouseleave", "unclustered-point", hideCursor);
      map.on("mouseleave", "timeline-view-point", hideCursor);

      function hideCursor() {
        map.getCanvas().style.cursor = "";
        // because we want to see the view persisted
        // popup.remove();
      }
      // for showing individual asset popups on hover
      function showPopup(e) {
        map.getCanvas().style.cursor = "pointer";

        let coordinates = e.features[0].geometry.coordinates.slice();

        // Ensure that if the map is zoomed out such that
        // multiple copies of the feature are visible, the
        // popup appears over the copy being pointed to.
        while (Math.abs(e.lngLat.lng - coordinates[0]) > 180) {
          coordinates[0] += e.lngLat.lng > coordinates[0] ? 360 : -360;
        }

        let html = "";
        if (e.features[0].properties.id !== undefined) {
          html =
            `${html}<p style='margin-bottom: 0;'><strong>${e.features[0].properties.id}</strong></p>`;
        }
        if (e.features[0].properties.assetType !== undefined) {
          html = `${html}<span><span class="popup-header">Type:</span>${e.features[0].properties.assetType}</span><br/>`;
        }
        if (e.features[0].properties.title !== undefined) {
          html = `${html}<span><span class="popup-header">Title:</span>${e.features[0].properties.title}</span><br/>`;
        }
        if (e.features[0].properties.description !== undefined) {
          html = `${html}<span><span class="popup-header">Description:</span>${e.features[0].properties.description}</span><br/>`;
        }
        if (e.features[0].properties.timestamp !== undefined) {
          html = `${html}<span><span class="popup-header">Last Update:</span>${new Date(
            Date.parse(e.features[0].properties.timestamp + "Z")
          )}</span><br>`;
        }
        let linkMade = false;
        if (html === "") {
          html = `${html}<p>No Data</p>`;
        } else {
          if (e.features[0].properties.viewType !== "timeline") {
            html = `${html}<span class="link-container" id=${e.features[0].properties.id} assetId=${e.features[0].properties.id}>Show Timeline View</span>`;
            linkMade = true;
          }
        }
        popup.setLngLat(coordinates).setHTML(html).addTo(map);
        if (linkMade === true) {
          document
            .getElementById(e.features[0].properties.id)
            .addEventListener("click", function (e) {
              e.preventDefault();
              popup.remove();
              console.log(e.target.id);
              getHistoryData(e.target.id);
            });
        }
      }

      localStorage.removeItem("first-load");
    })
    .then(() => {
      console.log("here");

      if (
        localStorage.getItem("first-load") === undefined ||
        localStorage.getItem("first-load") === null ||
        localStorage.getItem("first-load") === true ||
        localStorage.getItem("first-load") === "true"
      ) {
        console.log("First load of map");
        getAssetData(100);

        localStorage.setItem("first-load", "false");

        configureReloadForAssetView(5 * 60 * 1000);
      }
    });
});

// check which map layers are visible
function areGivenLayersActive(layerNames) {
  let canTriggerReloadFunction = false;
  if (layerNames !== undefined && layerNames.length > 0) {
    for (let i = 0; i < layerNames.length; i++) {
      let clickedLayer = layerNames[i];
      let visibility = map.getLayoutProperty(clickedLayer, "visibility");
      if (visibility !== undefined && visibility === "visible") {
        canTriggerReloadFunction = true;
      }
    }
  }
  return canTriggerReloadFunction;
}

// for configuring auto-refresh of asset history
function configureReloadForTimelineView(delay) {
  if (delay === undefined || isNaN(delay)) {
    delay = 5 * 60 * 1000; // 5 minutes by default
    console.log("History - Using Default Reload Value of 5 minutes");
  }

  let intervalId = setInterval(() => {
    let canTriggerReloadFunction = areGivenLayersActive(timelineViewLayers);
    if (canTriggerReloadFunction) {
      showPopupNotification("Reloading Data");
      let assetId = localStorage.getItem("current-asset-id");
      getHistoryData(assetId);
    } else {
      console.log("No Reload For History View");
    }
  }, delay);

  localStorage.setItem("history-interval", intervalId.toString());

  console.log(Number(localStorage.getItem("history-interval")));
}

// for configuring auto-refresh of all assets view
function configureReloadForAssetView(delay) {
  if (delay === undefined || isNaN(delay)) {
    delay = 5 * 60 * 1000; // 5 minutes by default
    console.log("Asset - Using Default Reload Value of 5 minutes");
  }

  let intervalId = setInterval(() => {
    let canTriggerReloadFunction = areGivenLayersActive(assetViewLayers);
    if (canTriggerReloadFunction) {
      showPopupNotification("Reloading Data");
      handleSubmit(new Event('na'))
      // getAssetData(100);
    } else {
      console.log("No Reload For Asset View");
    }
  }, delay);

  localStorage.setItem("asset-interval", intervalId.toString());

  console.log(Number(localStorage.getItem("asset-interval")));
}

function addSourceAndLayersForAssetView() {
  // Add a new source from our GeoJSON data and
  // set the 'cluster' option to true. GL-JS will
  // add the point_count property to your source data.
  /* THE SOURCE LAYER FOR ASSET TRACKING DATA */
  map.addSource("asset-tracking-data", {
    type: "geojson",
    // Point to GeoJSON data. This example visualizes all M1.0+ asset-tracking-data
    // from 12/22/15 to 1/21/16 as logged by USGS' Earthquake hazards program.
    data: defaultGeoJsonForAssetView,
    //   "https://docs.mapbox.com/mapbox-gl-js/assets/asset-tracking-data.geojson",
    cluster: true,
    clusterMaxZoom: 14, // Max zoom to cluster points on
    clusterRadius: defaultClusterRadius, // Radius of each cluster when clustering points (defaults to 50)
    clusterProperties: {
      has_truck: ["any", ["==", ["get", "assetType"], "truck"], "false"],
      has_salesperson: [
        "any",
        ["==", ["get", "assetType"], "salesperson"],
        "false",
      ],
      only_truck: ["all", ["==", ["get", "assetType"], "truck"], "false"],
      only_salesperson: [
        "all",
        ["==", ["get", "assetType"], "salesperson"],
        "false",
      ],
      truck: ["+", ["case", truck, 1, 0]],
      salesperson: ["+", ["case", salesperson, 1, 0]],
      others: ["+", ["case", others, 1, 0]],
    },
  });

  map.addLayer({
    id: "clusters",
    type: "circle",
    source: "asset-tracking-data",
    filter: ["has", "point_count"],
   
    paint: {
      "circle-color": [
        "step",
        ["get", "point_count"],
        "#51bbd6",
        100,
        "#f1f075",
        750,
        "#f28cb1",
      ],
      "circle-radius": ["step", ["get", "point_count"], 20, 100, 30, 750, 40],
    },
    layout: {
      visibility: "none",
    },
  });

  map.addLayer({
    id: "cluster-count",
    type: "symbol",
    source: "asset-tracking-data",
    filter: ["has", "point_count"],
    layout: {
      "text-field": "{point_count_abbreviated}",
      "text-font": ["DIN Offc Pro Medium", "Arial Unicode MS Bold"],
      "text-size": 12,
      visibility: "none",
    },
  });

  map.addLayer({
    id: "unclustered-point",
    type: "symbol",
    source: "asset-tracking-data",
    filter: ["!", ["has", "point_count"]],
    layout: {
      "icon-image": [
        "coalesce",
        ["image", "custom-marker"],
        ["image", "rocket-15"],
      ],
      "icon-allow-overlap": true,
      "icon-size": 0.6,
      visibility: "none",
    },
  });
}

function addSourceAndLayersForTimelineView() {
  addSourceAndLayerForRoute();
  addSourceAndLayerForAssetTimeLineView();
}

function addSourceAndLayerForRoute() {
  map.addSource("route-line-string", {
    type: "geojson",
    data: defaultLineString,
    
  });
  map.addLayer({
    id: "route-line-string-view",
    type: "line",
    source: "route-line-string",
    layout: {
      "line-join": "round",
      "line-cap": "round",
      visibility: "none",
    },
    paint: {
      "line-color": "#888",
      // "line-color": "#f28cb1",
      // "line-color": "red",
      "line-width": 8,
    },
  });
}

function addSourceAndLayerForAssetTimeLineView() {
  map.addSource("timeline-view", {
    type: "geojson",
    // data: timelineViewGeoJsonData,
    data: defaultGeoJsonTimelineView,
  });

  map.addLayer({
    id: "timeline-view-polygon",
    type: "fill",
    source: "timeline-view",
    paint: {
      "fill-color": "#888888",
      "fill-opacity": 0.3,
    },
    filter: ["==", "$type", "Polygon"],
    layout: {
      visibility: "none",
    },
  });

  map.addLayer({
    id: "timeline-view-linestring-route",
    type: "line",
    source: "timeline-view",
    layout: {
      "line-join": "round",
      "line-cap": "round",
      visibility: "none",
    },
    paint: {
      "line-color": "#888",
      "line-width": 6,
    },
    filter: ["==", "$type", "LineString"],
  });

  map.addLayer({
    id: "timeline-view-point",
    type: "symbol",
    source: "timeline-view",
    filter: ["==", "$type", "Point"],
    layout: {
      // "icon-image": "custom-marker",
      "icon-image": [
        "coalesce",
        ["image", "custom-marker"],
        ["image", "rocket-15"],
      ],
      "icon-allow-overlap": true,
      "icon-size": 0.5,
      visibility: "none",
    },
  });
}

function addLayerForHeatmapView() {
  map.addLayer(
    {
      id: "asset-view-heat-map",
      type: "heatmap",
      source: "asset-tracking-data",
      maxzoom: 9,
      paint: {
        // Increase the heatmap weight based on frequency and property magnitude
        "heatmap-weight": [
          "interpolate",
          ["linear"],
          ["get", "point_count"],
          0,
          0,
          6,
          1,
        ],
        // Increase the heatmap color weight weight by zoom level
        // heatmap-intensity is a multiplier on top of heatmap-weight
        "heatmap-intensity": ["interpolate", ["linear"], ["zoom"], 0, 1, 9, 3],
        // Color ramp for heatmap.  Domain is 0 (low) to 1 (high).
        // Begin color ramp at 0-stop with a 0-transparancy color
        // to create a blur-like effect.
        "heatmap-color": [
          "interpolate",
          ["linear"],
          ["heatmap-density"],
          0,
          "rgba(33,102,172,0)",
          0.2,
          "rgb(39,113,157)",
          0.4,
          "rgb(210,206,31)",
          0.6,
          "rgb(152,86,48)",
          0.8,
          "rgb(81,30,9)",
          1,
          "rgb(178,24,43)",
        ],
        // Adjust the heatmap radius by zoom level
        "heatmap-radius": ["interpolate", ["linear"], ["zoom"], 0, 2, 9, 20],
        // Transition from heatmap to circle layer by zoom level
        "heatmap-opacity": ["interpolate", ["linear"], ["zoom"], 7, 1, 9, 0],
      },
      layout: {
        visibility: "none",
      },
    },
    // add layer before given layer
    "waterway-label"
  );

  map.addLayer(
    {
      id: "asset-view-heat-map-point",
      type: "circle",
      source: "asset-tracking-data",
      minzoom: 7,
      paint: {
        "circle-radius": [
          "interpolate",
          ["linear"],
          ["zoom"],
          7,
          ["interpolate", ["linear"], ["get", "point_count"], 1, 1, 6, 4],
          16,
          ["interpolate", ["linear"], ["get", "point_count"], 1, 5, 6, 50],
        ],
        "circle-color": [
          "interpolate",
          ["linear"],
          ["get", "point_count"],
          1,
          "rgba(33,102,172,0)",
          2,
          "rgb(103,169,207)",
          3,
          "rgb(209,229,240)",
          4,
          "rgb(253,219,199)",
          5,
          "rgb(239,138,98)",
          6,
          "rgb(178,24,43)",
        ],
        "circle-stroke-color": "white",
        "circle-stroke-width": 1,
        // Transition from heatmap to circle layer by zoom level
        "circle-opacity": ["interpolate", ["linear"], ["zoom"], 7, 0, 8, 1],
      },
      layout: {
        visibility: "none",
      },
    },
    "waterway-label"
  );
}
