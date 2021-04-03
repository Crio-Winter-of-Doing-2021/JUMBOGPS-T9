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

let polygon;
let route;
let sourcePoint;

document.getElementById("polygon").addEventListener("click", function (e) {
  let data = draw.getAll();
  console.log(data);
  if (data.features.length == 1) {
    console.log("single feature found");
    if (data.features[0].geometry.type == "Polygon") {
      polygon = data.features[0];
      document.querySelector("#asset-geofence").checked = true;
    } else {
      triggerIframe("Invalid Polygon");
      // call clear all
      // delete yourself
    }
  } else {
    triggerIframe("Only 1 feature allowed at a time");
    // call clear all
    // delete yourself
  }
});

document.getElementById("route").addEventListener("click", function (e) {
  let data = draw.getAll();
  console.log(data);
  if (data.features.length == 1) {
    console.log("single feature found");
    if (data.features[0].geometry.type == "LineString") {
      route = data.features[0];
      document.querySelector("#asset-route").checked = true;
    } else {
      triggerIframe("Invalid Route");
      // call clear all
      // delete yourself
    }
  } else {
    triggerIframe("Only 1 feature allowed at a time");
    // call clear all
    // delete yourself
  }
});

document.getElementById("source-point").addEventListener("click", function (e) {
  let data = draw.getAll();
  console.log(data);
  if (data.features.length == 1) {
    console.log("single feature found");
    if (data.features[0].geometry.type == "Point") {
      sourcePoint = data.features[0];
      document.querySelector("#asset-source-point").checked = true;
    } else {
      triggerIframe("Invalid Point");
      // call clear all
      // delete yourself
    }
  } else {
    triggerIframe("Only 1 feature allowed at a time");
    // call clear all
    // delete yourself
  }
});

document.getElementById("show-all").addEventListener("click", function (e) {
  let allStoredFeatures = [];
  if (polygon !== undefined) {
    allStoredFeatures.push(polygon);
  }
  if (route !== undefined) {
    allStoredFeatures.push(route);
  }
  if (sourcePoint !== undefined) {
    allStoredFeatures.push(sourcePoint);
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
