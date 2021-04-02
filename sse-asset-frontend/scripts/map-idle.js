// map.on('style.load', () => {
// 	const waiting = () => {
// 		if (!map.isStyleLoaded()) {
// 			setTimeout(waiting, 200);
// 		} else {
// 			loadMyLayers();
// 		}
// 	};
// 	waiting();
// });


map.on("idle", function () {
  // If these two layers have been added to the style,
  // add the toggle buttons.
  // Enumerate ids of the layers.

  // if (
  //   localStorage.getItem("first-load") === undefined ||
  //   localStorage.getItem("first-load") === null ||
  //   localStorage.getItem("first-load") === true ||
  //   localStorage.getItem("first-load") === "true"
  // ) {
  //   console.log('First load of map')
	// 	map.on('style.load', () => {
	// 		getAssetData(100);
	// 	})
	//
  //   localStorage.setItem("first-load", "false");
	//
  //   // setInterval(getAssetData(100) ,1000);
	//
  // }

  let toggleableLayerIds = [
    "Asset View",
    // "timeline-view",
    // "timeline-view-with-polygon",
    "Show Heatmap",
  ];
  // Set up the corresponding toggle button for each layer.
  for (let i = 0; i < toggleableLayerIds.length; i++) {
    let id = toggleableLayerIds[i];
    // Create a link.
    if (!document.getElementById(id)) {
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

        if (clickedLayer === "Asset View") {
          // showAssetView(assetViewGeoJson);
          // showAssetView(assetDataFromAPI);
          hideLayers(timelineViewLayers);
          hideLayers(heatmapLayers);
          showLayers(assetViewLayers);
        } else if (clickedLayer === "timeline-view-with-polygon") {
          // showTimeLineView(multipleGeometryJson);
          showTimeLineView(historyDataFromAPIExtended);
        } else if (clickedLayer === "timeline-view") {
          // showTimeLineView(dummyGeoJson);
          showTimeLineView(historyDataFromAPI);
        } else if (clickedLayer === "Show Heatmap") {
          // map.getSource("asset-tracking-data").setData('https://docs.mapbox.com/mapbox-gl-js/assets/earthquakes.geojson');
          // map.fitBounds(turf.bbox(dummyGeoJson), {
          //   padding: 40,
          //   maxZoom: 7,
          // });
          hideLayers(assetViewLayers);
          hideLayers(timelineViewLayers);
          showLayers(heatmapLayers);
        }
      };

      let layers = document.getElementById("menu");
      layers.appendChild(link);
    }
  }
});
