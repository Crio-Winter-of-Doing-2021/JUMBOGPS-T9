const mainUrl = 'https://jumbogps.anugrahsinghal.repl.co/assets';
const getAllAssetsUrl = `${mainUrl}?limit=100`;
const getAssetHistoryUrl = 'https://jumbogps.anugrahsinghal.repl.co/assets/2555/history';

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
	let assets=[], centroid;

	await axios.get(url)
	.then((body) => {
		if(body.data.assets){
			assets = body.data.assets
		}
		else
		assets.push(body.data);

		centroid = body.data.centroid?body.data.centroid:body.data.lastReportedLocation;
})
	.catch((err) => {
		console.log("no assets found!");
		return;
	});


	// long,lat of a random asset
	let { longitude, latitude } = centroid;

	// for recentering the map to a marker
	map.flyTo({
		center: [ longitude, latitude ],
		essential: true // this animation is considered essential with respect to prefers-reduced-motion
	});

	features = [];

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
                <div><a onclick="getAssetHistory('${asset.id}','${getAssetHistoryUrl}','history')">See last 24 hrs view</a></div>
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
		'https://docs.mapbox.com/mapbox-gl-js/assets/custom_marker.png',
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
						features: features
					}
				});
	
				// for adding diff map views
				map.addLayer({
					id: current,
					type: 'symbol',
					source: current,
					layout: {
						'icon-image': 'custom-marker',
						'icon-allow-overlap': true,
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

	for(let layer of layers) createAssetPopup(layer)
}

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

// for searching asset by timestamp
function searchByTimeStamp(url,layer) {
	getAssetData(url, layer);
}

// for searching assets by type
function searchByType(url,layer){
	getAssetData(url,layer);
}

// for searching assets by id
function searchById(url,layer){
	getAssetData(url,layer);
}


// for getting loc history of an asset
async function getAssetHistory(assetId,url,current){
	let asset,centroid,history;

	let searchByIdUrl = `${mainUrl}/${assetId}`;

	await axios.get(searchByIdUrl)
	.then((body) => {
		
		asset = body.data;
	centroid = body.data.centroid?body.data.centroid:body.data.lastReportedLocation;
})
	.catch((err) => console.log(err));

	let {id,assetType,description,title} = asset;


	// long,lat of a random asset
	let { longitude, latitude } = centroid;

	// for recentering the map to a marker
	map.flyTo({
		center: [ longitude, latitude ],
		essential: true // this animation is considered essential with respect to prefers-reduced-motion
	});

	features = [];

	await axios.get(url)
	.then(body => history = body.data)
	.catch(err => console.log(err));

	history.forEach((record) => {
		// console.log(asset);
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


const form = document.getElementsByTagName("form")[0];
const searchFilter = document.getElementsByName("criteria")[0];
const startTimeFilter = document.getElementsByName("startTime")[0];
const maxAssetsCount = document.getElementsByName("maxAssetsCount")[0];
const endTimeFilter = document.getElementsByName("endTime")[0];
const assetsCount = document.querySelector("#assetsCount");
const assetsTypes = document.querySelector("#assetsType");
const assetsShown = document.querySelector(".sec2 span");

form.addEventListener("submit",handleSubmit);

// function to handle form submit
function handleSubmit(e){
	e.preventDefault();

	let searchType = searchFilter.value.trim();
	let searchId = parseInt(searchType);
	let startTimeValue = startTimeFilter.value;
	let endTimeValue = endTimeFilter.value;
	let count = maxAssetsCount.value;

	assetsShown.innerHTML = count;

	let startTime = getUTCTime(startTimeValue);
	let endTime = getUTCTime(endTimeValue);


	if(Number.isInteger(searchId)){
		let searchByIdUrl = `${mainUrl}/${searchId}`;

		searchById(searchByIdUrl,"byId");
	}

	else{

		let searchByTimeUrl = `${mainUrl}?limit=${count}&type=${searchType}&startTimeStamp=${startTime}&endTimeStamp=${endTime}`;

		searchByTimeStamp(searchByTimeUrl,'byTime');

	}

}

// for converting local time to GMT
function getUTCTime(time){
	let datetime = new Date(time);
	datetime.setHours(datetime.getHours()+5); 
	datetime.setMinutes(datetime.getMinutes()+30);
	
	return Date.parse(datetime).toString().slice(0,-3);
}

// function for populating more info section
async function getMapInfo(){
	console.log(assetsCount.innerHTML);

	let assets,typeArr=[];

	await axios.get(mainUrl)
	.then((body) => assets = body.data.assets)
	.catch((err)=> console.log(err));

	assetsCount.innerHTML=assets.length;

	assets.forEach((asset)=>{
		let type = asset.assetType.toLowerCase();
		if(typeArr.indexOf(type)==-1){
			typeArr.push(type);
			assetsTypes.innerHTML += `${type}, `;
		}
	})

	assetsTypes.innerHTML.slice(0,-2);
}


getMapInfo();

// index view
getAllAssets(getAllAssetsUrl,'all');