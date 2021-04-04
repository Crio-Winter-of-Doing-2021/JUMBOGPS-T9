map.on("idle", function () {

  let toggleableLayerIds = [
    "Asset View",
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
          showOrHideLayers(timelineViewLayers,"visible","none");
          showOrHideLayers(heatmapLayers,"visible","none");
          showOrHideLayers(assetViewLayers,"none","visible");
        } else if (clickedLayer === "timeline-view-with-polygon") {
          showTimeLineView(historyDataFromAPIExtended);
        } else if (clickedLayer === "timeline-view") {
          showTimeLineView(historyDataFromAPI);
        } else if (clickedLayer === "Show Heatmap") {
         
          showOrHideLayers(assetViewLayers,"visible","none");
          showOrHideLayers(timelineViewLayers,"visible","none");
          showOrHideLayers(heatmapLayers,"none","visible");
        }
      };

      let layers = document.getElementById("menu");
      layers.appendChild(link);
    }
  }
});
