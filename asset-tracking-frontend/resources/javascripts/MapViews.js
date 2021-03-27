const mainUrl = 'https://jumbogps-auth-sse.anugrahsinghal.repl.co/assets';
const getAllAssetsUrl = `${mainUrl}?limit=100`;

mapboxgl.accessToken = MAPBOX_TOKEN;

let map = new mapboxgl.Map({
	container: 'map',
	style: 'mapbox://styles/mapbox/streets-v11',
	zoom: 3
});

let features = [];
const layers = ['byTime','byType','byId','all','history'];

// for getting asset data from appropriate API call
async function getAssetData(url,current) {
	
	let token = localStorage.getItem("token");

	let assets=[], centroid;
	
	await axios.get(url,{
		"headers": {
			"Authorization": `Bearer ${token}`
		  }
	})
	.then((body) => {
		if(body.data.assets){
			assets = body.data.assets
		}
		else
		assets.push(body.data);

		centroid = body.data.centroid?body.data.centroid:body.data.lastReportedLocation;
})
	.catch((err) => {
		if(err.response){
			alert("no assets found for that id/type");
			throw new Error("no assets");
		}
	});

	// long,lat of a random asset
	let { longitude, latitude } = centroid;

	// for recentering the map to a marker
	map.flyTo({
		center: [ longitude, latitude ],
		essential: true // this animation is considered essential with respect to prefers-reduced-motion
	});

	features = [],features1=[];

	assets.forEach((asset) => {
		// console.log(asset);
		let long = asset.lastReportedLocation.longitude;
		let lat = asset.lastReportedLocation.latitude;

		features.push({
			type: 'Feature',
			properties: {
				description: `<h4>${asset.title}</h4>
                <span><strong>Type:</strong> ${asset.assetType}</span>
                <p>${asset.description}</p>
                <span>${asset.lastReportedTimestamp}</span>
                <div><a onclick="getAssetHistory('${asset.id}','history')">See last 24 hrs view</a></div>
                `
			},
			geometry: {
				type: 'Point',
				coordinates: [ long, lat ]
			}
		});
	
	});

	addMapLayer(current);
}

// getting details popup of an asset
function addMapLayer(current) {

	map.loadImage(
		"https://docs.mapbox.com/mapbox-gl-js/assets/custom_marker.png",
	
		// Add an image to use as a custom marker
		function(error, image) {
			if (error) throw error;

			if(map.hasImage('custom-marker'))
			map.removeImage('custom-marker', image);
			map.addImage('custom-marker', image);

			if(map.getSource(current) && map.getLayer(current)){
				map.removeLayer(current);
				map.removeSource(current);
			}
			map.addSource(current, {
				type: 'geojson',
					data: {
						type: 'FeatureCollection',
						features
					}
				});
	
				// for adding diff map views
				map.addLayer({
					id: current,
					type: 'symbol',
					source: current,
					layout: {
						'icon-image': "custom-marker",
						'icon-allow-overlap': true,
						'icon-size':0.6,
						visibility: 'visible'
					}
				});
				
				for(let layer of layers){
					if(layer!=current && map.getSource(layer) && map.getLayer(layer)){
						map.removeLayer(layer);
						map.removeSource(layer);
				}
			}
		}
	);


}

for(let layer of layers) createAssetPopup(layer)

function createAssetPopup(layer) {
	// Create a popup, but don't add it to the map yet.
	let popup = new mapboxgl.Popup();

	map.on('mouseenter', layer, function(e) {
		// Change the cursor style as a UI indicator.
		map.getCanvas().style.cursor = 'pointer';

		let coordinates = e.features[0].geometry.coordinates.slice();
		let description = e.features[0].properties.description;

		// Ensure that if the map is zoomed out such that multiple
		// copies of the feature are visible, the popup appears
		// over the copy being pointed to.
		while (Math.abs(e.lngLat.lng - coordinates[0]) > 180) {
			coordinates[0] += e.lngLat.lng > coordinates[0] ? 360 : -360;
		}

		// Populate the popup and set its coordinates
		// based on the feature found.
		popup.setLngLat(coordinates).setHTML(description).addTo(map);
	});

	popup.on('mouseleave', layer, function() {
		map.getCanvas().style.cursor = '';
		popup.remove();
	});
}

// for getting all assets available
async function getAllAssets(url,layer){
	await getAssetData(url,layer);
}

// for getting loc history of an asset
async function getAssetHistory(assetId,current){
	let asset,centroid,history;

	let searchByIdUrl = `${mainUrl}/${assetId}`;

	await axios.get(searchByIdUrl)
	.then((body) => {
		
		asset = body.data;
	centroid = body.data.centroid?body.data.centroid:body.data.lastReportedLocation;
})
	.catch((err) =>{
		alert("no history for that asset found.try another asset.");
	});

	let {id,assetType,description,title} = asset;


	// long,lat of a random asset
	let { longitude, latitude } = centroid;

	// for recentering the map to a marker
	map.flyTo({
		center: [ longitude, latitude ],
		zoom:8,
		essential: true // this animation is considered essential with respect to prefers-reduced-motion
	});

	features = [];

	let getAssetHistoryUrl = `${mainUrl}/${assetId}/history`;

	await axios.get(getAssetHistoryUrl)
	.then(body => history = body.data)
	.catch(err => console.log(err));

	if(history.length==0){
		alert("no history found for that asset");
		return;
	}
	history.forEach((record) => {
		let long = record.location.longitude;
		let lat = record.location.latitude;

		features.push({
			type: 'Feature',
			properties: {
				description: `<h4>${title}</h4>
                <span><strong>Type:</strong> ${assetType}</span>
                <p>${description}</p>
                <span>${record.timestamp}</span>
                `
			},
			geometry: {
				type: 'Point',
				coordinates: [ long, lat ]
			}
		});
	});

	addMapLayer(current);
}

// index view
getAllAssets(getAllAssetsUrl,'all');

