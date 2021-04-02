map.on("idle", function () {
  // If these two layers have been added to the style,
  // add the toggle buttons.
  // Enumerate ids of the layers.

  if (
    localStorage.getItem("first-load") === undefined ||
    localStorage.getItem("first-load") === null ||
    localStorage.getItem("first-load") === true ||
    localStorage.getItem("first-load") === "true"
  ) {
    console.log('First load of map')
    getAssetData(100);
    localStorage.setItem("first-load", "false");

    // setInterval(getAssetData(100) ,1000);

  }

  let toggleableLayerIds = [
    "asset-view",
    "timeline-view",
    "timeline-view-with-polygon",
    "heatmap-layer",
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

        if (clickedLayer === "asset-view") {
          // showAssetView(assetViewGeoJson);
          showAssetView(assetDataFromAPI);
          showPopupNotification("100 assets found");
        } else if (clickedLayer === "timeline-view-with-polygon") {
          // showTimeLineView(multipleGeometryJson);
          showTimeLineView(historyDataFromAPIExtended);
        } else if (clickedLayer === "timeline-view") {
          // showTimeLineView(dummyGeoJson);
          showTimeLineView(historyDataFromAPI);
        } else if (clickedLayer === "heatmap-layer") {
          map.getSource("asset-tracking-data").setData(dummyGeoJson);
          map.fitBounds(turf.bbox(dummyGeoJson), {
            padding: 40,
            maxZoom: 7,
          });
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
