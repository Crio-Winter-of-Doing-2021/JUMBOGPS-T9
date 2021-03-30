function showThePopup(e) {
  map.getCanvas().style.cursor = "pointer";

  let coordinates = e.features[0].geometry.coordinates.slice();

  // Ensure that if the map is zoomed out such that
  // multiple copies of the feature are visible, the
  // popup appears over the copy being pointed to.
  while (Math.abs(e.lngLat.lng - coordinates[0]) > 180) {
    coordinates[0] += e.lngLat.lng > coordinates[0] ? 360 : -360;
  }

  let html = "";
  html = html + "<p>" + e.features[0].properties.id + "</p>";
  html =
    html +
    "<span><strong>Type:</strong>" +
    e.features[0].properties.title +
    "</span>";
  html = html + "<p>" + e.features[0].properties.type + "</p>";
  html =
    html + "<span>" + e.features[0].properties.description + "</span><br/>";
  html = html + "<span>" + e.features[0].properties.timestamp + "</span><br>";

  new mapboxgl.Popup({ closeOnMove: true })
    .setLngLat(coordinates)
    .setHTML(html)
    .addTo(map);
}



function showtimelineViewForAsset(timelineViewGeoJsonData) {
  makeLineLayerOnMap(timelineViewGeoJsonData);
}

function renderMapForAssetViewData(centroid, geoJsonData) {
  console.log(geoJsonData);
  // for recentering the map to a marker
  map.flyTo({
    center: centroid.coordinates,
    essential: true, // this animation is considered essential with respect to prefers-reduced-motion
    zoom: 1,
    bearing: 0,

    // These options control the flight curve, making it move
    // slowly and zoom out almost completely before starting
    // to pan.
    speed: 0.2, // make the flying slow
    curve: 1, // change the speed at which it zooms out
  });
}

function makeLineLayerOnMap(timelineViewGeoJsonData) {
  map.addSource("route-line-string", {
    type: "geojson",
    data: defaultLineString,
    // data: makeLineStringForGeoJsonTimelineView(timelineViewGeoJsonData),
    // <!-- #region data other forms -->
    // data: turf.lineString(
    //   makeLineStringForGeoJsonTimelineView(timelineViewGeoJsonData)
    //     .geometry.coordinates
    // ),
    // data with linestring data

    /*
          data: {
                    type: "Feature",
                    properties: {},
                    geometry: {
                      type: "LineString",
                      coordinates: [
                        [-122.48369693756104, 37.83381888486939],
                        [-122.48348236083984, 37.83317489144141],
                        [-122.48339653015138, 37.83270036637107],
                        [-122.48356819152832, 37.832056363179625],
                        [-122.48404026031496, 37.83114119107971],
                        [-122.48404026031496, 37.83049717427869],
                        [-122.48348236083984, 37.829920943955045],
                        [-122.48356819152832, 37.82954808664175],
                        [-122.48507022857666, 37.82944639795659],
                        [-122.48610019683838, 37.82880236636284],
                        [-122.48695850372314, 37.82931081282506],
                        [-122.48700141906738, 37.83080223556934],
                        [-122.48751640319824, 37.83168351665737],
                        [-122.48803138732912, 37.832158048267786],
                        [-122.48888969421387, 37.83297152392784],
                        [-122.48987674713133, 37.83263257682617],
                        [-122.49043464660643, 37.832937629287755],
                        [-122.49125003814696, 37.832429207817725],
                        [-122.49163627624512, 37.832564787218985],
                        [-122.49223709106445, 37.83337825839438],
                        [-122.49378204345702, 37.83368330777276],
                      ],
                    },
                  },

          <!-- #endregion -->
          */
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

  map.addSource("timeline-view", {
    type: "geojson",
    // data: timelineViewGeoJsonData,
    data: defaultGeoJsonTimelineView,
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
      "line-color": "#f28cb1",
      "line-width": 8,
    },
    filter: ["==", "$type", "LineString"],
  });

  hideLayers(assetViewLayers);
  showLayers(timelineViewLayers);
  /* <!-- #region flyto -->

        map.flyTo({
          center: getCenterForTimeLineView(timelineViewGeoJsonData),
          essential: true, // this animation is considered essential with respect to prefers-reduced-motion
          zoom: 14,
          bearing: 0,

          // These options control the flight curve, making it move
          // slowly and zoom out almost completely before starting
          // to pan.
          speed: 0.8, // make the flying slow
          curve: 1, // change the speed at which it zooms out
        });

        <!-- #endregion --> */

  // map.fitBounds(turf.bbox(timelineViewGeoJsonData), { padding: 20 });
}

function createLayerFilterOnIdleMap() {
  map.on("idle", function () {
    // If these two layers have been added to the style,
    // add the toggle buttons.
    if (map.getLayer("clusters") && map.getLayer("unclustered-point")) {
      // Enumerate ids of the layers.
      let toggleableLayerIds = ["clusters", "unclustered-point"];
      // Set up the corresponding toggle button for each layer.
      for (let i = 0; i < toggleableLayerIds.length; i++) {
        let id = toggleableLayerIds[i];
        if (!document.getElementById(id)) {
          // Create a link.
          let link = document.createElement("a");
          link.id = id;
          link.href = "#";
          link.textContent = id;
          link.className = "active";
          // Show or hide layer when the toggle is clicked.
          link.onclick = function (e) {
            let clickedLayer = this.textContent;
            e.preventDefault();
            e.stopPropagation();

            let visibility = map.getLayoutProperty(clickedLayer, "visibility");

            // Toggle layer visibility by changing the layout object's visibility property.
            if (visibility === "visible") {
              map.setLayoutProperty(clickedLayer, "visibility", "none");
              this.className = "";
            } else {
              this.className = "active";
              map.setLayoutProperty(clickedLayer, "visibility", "visible");
            }
          };

          let layers = document.getElementById("menu");
          layers.appendChild(link);
        }
      }
    }
  });
} /* } function end*/

function getCenterForTimeLineView(timelineViewGeoJsonData) {
  let len = timelineViewGeoJsonData.features.length;
  let mid = Math.floor(len / 2);
  return timelineViewGeoJsonData.features[mid].geometry.coordinates;
}
