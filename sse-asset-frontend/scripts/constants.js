/* #region  CONSTANTS AND HELPER FUNCTIONS + DATA */

const mapboxAccessToken =
  "pk.eyJ1IjoiYW51Z3JhaHNpbmdoYWwiLCJhIjoiY2tscW1mcjcwMWVjaDJ2bjFmYml5cmNpNSJ9.1M6Wd3NEDq95YhoCyMxU8A";
const defaultCentroid = [0, 0];
const defaultGeoJsonForAssetView = {
  type: "FeatureCollection",
  features: {
    type: "Feature",
    properties: {},
    geometry: {
      type: "Point",
      coordinates: [0, 0],
    },
  },
};
const defaultLineString = {
  type: "Feature",
  properties: {},
  geometry: {
    type: "LineString",
    coordinates: [],
  },
};
const defaultGeoJsonTimelineView = {
  type: "FeatureCollection",
  features: [
    {
      type: "Feature",
      geometry: {
        type: "Polygon",
        coordinates: [[]],
      },
    },
    {
      type: "Feature",
      geometry: {
        type: "Point",
        coordinates: [],
      },
    },
    {
      type: "Feature",
      properties: {},
      geometry: {
        type: "LineString",
        coordinates: [],
      },
    },
  ],
};
const assetViewLayers = ["clusters", "cluster-count", "unclustered-point"];
const timelineViewLayers = [
  "timeline-view-point",
  "timeline-view-polygon",
  "timeline-view-linestring-route",
  "route-line-string-view",
];
const heatmapLayers = [
  "unclustered-point",
  "asset-view-heat-map",
  "asset-view-heat-map-point",
];
const defaultClusterRadius = 50;
const coordinatesGeocoder = function (query) {
  // match anything which looks like a decimal degrees coordinate pair
  let matches = query.match(
    /^[ ]*(?:Lat: )?(-?\d+\.?\d*)[, ]+(?:Lng: )?(-?\d+\.?\d*)[ ]*$/i
  );
  if (!matches) {
    return null;
  }

  function coordinateFeature(lng, lat) {
    return {
      center: [lng, lat],
      geometry: {
        type: "Point",
        coordinates: [lng, lat],
      },
      place_name: "Lat: " + lat + " Lng: " + lng,
      place_type: ["coordinate"],
      properties: {},
      type: "Feature",
    };
  }

  let coord1 = Number(matches[1]);
  let coord2 = Number(matches[2]);
  let geocodes = [];

  if (coord1 < -90 || coord1 > 90) {
    // must be lng, lat
    geocodes.push(coordinateFeature(coord1, coord2));
  }

  if (coord2 < -90 || coord2 > 90) {
    // must be lat, lng
    geocodes.push(coordinateFeature(coord2, coord1));
  }

  if (geocodes.length === 0) {
    // else could be either lng, lat or lat, lng
    geocodes.push(coordinateFeature(coord1, coord2));
    geocodes.push(coordinateFeature(coord2, coord1));
  }

  return geocodes;
};

const salesperson = ["==", ["get", "assetType"], "SALESPERSON"];
const truck = ["==", ["get", "assetType"], "TRUCK"];
const others = [
  "any",
  ["==", ["get", "assetType"], "Cogeneration"],
  ["==", ["get", "assetType"], ""],
];

const mainUrl = 'https://jumbogps-geo.anugrahsinghal.repl.co/assets';
/* #endregion */
