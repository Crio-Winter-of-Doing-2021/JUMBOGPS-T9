mapboxgl.accessToken =
  "pk.eyJ1IjoiYW51Z3JhaHNpbmdoYWwiLCJhIjoiY2tscW1mcjcwMWVjaDJ2bjFmYml5cmNpNSJ9.1M6Wd3NEDq95YhoCyMxU8A";
var map = new mapboxgl.Map({
  container: "map", // container id
  style: "mapbox://styles/mapbox/streets-v11",
  center: [78.476681027237, 22.1991660760527], // starting position
  zoom: 4, // starting zoom
});

/* given a query in the form "lng, lat" or "lat, lng" returns the matching
 * geographic coordinate(s) as search results in carmen geojson format,
 * https://github.com/mapbox/carmen/blob/master/carmen-geojson.md
 */
const geocoder = new MapboxGeocoder({
  accessToken: mapboxgl.accessToken,
  mapboxgl: mapboxgl,
  marker: false,
});

map.addControl(geocoder, "top-left");

map.addControl(new mapboxgl.NavigationControl(), "bottom-right");

document.querySelector("#admin-form").addEventListener("click", (e) => {
  e.preventDefault();
  e.stopPropagation();
});

document.querySelector("#asset-create-btn").addEventListener("click", (e) => {
  e.preventDefault();
  e.stopPropagation();

  console.log("create asset btn");
});

let opt = {
  displayControlsDefault: false,
  controls: {
    polygon: true,
    trash: true,
    line_string: true,
    point: true,
  },
  touchEnabled: true,
};

var draw = new MapboxDraw(opt);
map.addControl(draw);

map.on("draw.create", updateArea);
map.on("draw.delete", updateArea);
map.on("draw.update", updateArea);

function updateArea(e) {
  var data = draw.getAll();
  var answer = document.getElementById("calculated-area");
  if (data.features.length > 0) {
    var area = turf.area(data);
    // restrict to area to 2 decimal points
    var rounded_area = Math.round(area * 100) / 100;
    answer.innerHTML =
      "<p><strong>" + rounded_area + "</strong></p><p>square meters</p>";
  } else {
    answer.innerHTML = "";
    if (e.type !== "draw.delete")
      triggerIframe("Use the draw tools to draw a polygon!");
  }
}

let asset = {};

document.getElementById("polygon").addEventListener("click", function (e) {
  let data = draw.getAll();
  console.log(data);
  if (data.features.length == 1) {
    console.log("single feature found");
    if (data.features[0].geometry.type == "Polygon") {
      asset.geofence = data.features[0].geometry;
      makeCheckboxInputChecked("asset-geofence");
    } else {
      triggerIframe("Invalid Geometry");
      // call clear all
      // delete yourself
    }
  } else if (data.features.length == 0) {
    triggerIframe("No Polygons Drawn on map");
  } else {
    triggerIframe("Only 1 feature allowed at a time");
  }
});

document.getElementById("route").addEventListener("click", function (e) {
  let data = draw.getAll();
  console.log(data);
  if (data.features.length == 1) {
    console.log("single feature found");
    if (data.features[0].geometry.type == "LineString") {
      asset.route = data.features[0].geometry;
      makeCheckboxInputChecked("asset-route");
    } else {
      triggerIframe("Invalid Geometry");
      // call clear all
      // delete yourself
    }
  } else if (data.features.length == 0) {
    triggerIframe("No Routes Drawn on map");
  } else {
    triggerIframe("Only 1 feature allowed at a time");
  }
});

document.getElementById("source-point").addEventListener("click", function (e) {
  let data = draw.getAll();
  console.log(data);
  if (data.features.length == 1) {
    console.log("single feature found");
    if (data.features[0].geometry.type == "Point") {
      asset.location = {};
      asset.location.coordinates = data.features[0].geometry;
      makeCheckboxInputChecked("asset-source-point");
    } else {
      triggerIframe("Invalid Geometry");
      // call clear all
      // delete yourself
    }
  } else if (data.features.length == 0) {
    triggerIframe("No Point Drawn on map");
  } else {
    triggerIframe("Only 1 feature allowed at a time");
  }
});

document.getElementById("show-all").addEventListener("click", function (e) {
  let allStoredFeatures = [];
  if (asset.geofence !== undefined) {
    allStoredFeatures.push({ type: "Feature",properties:{}, geometry: asset.geofence });
  }
  if (asset.route !== undefined) {
    allStoredFeatures.push({ type: "Feature",properties:{}, geometry: asset.route });
  }
  if (asset.location.coordinates !== undefined) {
    allStoredFeatures.push({
      type: "Feature",
      properties:{},
      geometry: asset.location.coordinates,
    });
  }
  if (allStoredFeatures.length == 0) {
    console.log("no valid features");
    return;
  }

  draw.deleteAll();

  var ids = draw.set({
    type: "FeatureCollection",
    features: allStoredFeatures,
  });
});

function makeCheckboxInputChecked(checkboxId) {
  document.querySelector("#" + checkboxId).checked = true;
}

document
  .querySelector("#asset-create-btn")
  .addEventListener("click", handleCreateAsset);

function handleCreateAsset(e) {
  // get values of form and make asset object
  asset.assetType = document.querySelector("#asset-type").value;
  asset.title = document.querySelector("#asset-title").value;
  asset.description = document.querySelector("#asset-description").value;
  if (validateAssetProperties()) {
    asset.location.deviceTimestamp = Math.floor(Date.now() / 1000);
    console.log(asset);
    createAsset(asset).then(() => {
      clearForm();
    });

    console.log(asset);
  }
}

function uncheckAll() {
  const cbs = document.querySelectorAll('input[type="checkbox"]');
  cbs.forEach((cb) => {
    cb.checked = false;
  });
}

function clearForm() {
  document.querySelector("#asset-type").value = "";
  document.querySelector("#asset-title").value = "";
  document.querySelector("#asset-description").value = "";
  uncheckAll();
  draw.deleteAll();
  asset = {};
}

function validateAssetProperties() {
  if (asset.assetType === undefined || asset.assetType === "") {
    triggerIframe("Asset Type is needed");
    return false;
  }
  if (asset.location === undefined) {
    triggerIframe("Location Data is needed");
    return false;
  }
  if (
    asset.location !== undefined &&
    asset.location.coordinates === undefined
  ) {
    triggerIframe("Location Data is needed");
    return false;
  }
  if (asset.title === undefined || asset.title === "") {
    triggerIframe("Asset Title is needed");
    return false;
  }
  if (asset.description === undefined || asset.description === "") {
    triggerIframe("Asset description is needed");
    return false;
  }
  return true;
}
