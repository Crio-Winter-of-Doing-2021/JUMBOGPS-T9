const form = document.getElementsByTagName('form')[0];
const searchFilter = document.getElementsByName('criteria')[0];
const startTimeFilter = document.getElementsByName('startTime')[0];
const endTimeFilter = document.getElementsByName('endTime')[0];
const assetsCount = document.getElementsByName('maxAssetsCount')[0];
const errors = document.getElementsByClassName('error');

form.addEventListener('submit', handleSubmit);

// function to handle form submit
function handleSubmit(e) {
	e.preventDefault();

	let searchType = searchFilter.value.trim();
	let searchId = parseInt(searchType);
	let startTimeValue = startTimeFilter.value;
	let endTimeValue = endTimeFilter.value;
	let count = assetsCount.value;

	// if (validate(count, startTimeValue, endTimeValue, searchType)) return;

	let startTime =
		startTimeValue == endTimeValue ? getUTCTime(startTimeValue, true, 00) : getUTCTime(startTimeValue, false, 00);

	let endTime =
		startTimeValue == endTimeValue ? getUTCTime(endTimeValue, true, 59) : getUTCTime(endTimeValue, false, 00);

	if (Number.isInteger(searchId)) {
		let searchByIdUrl = `${mainUrl}/${searchId}`;

		getAllAssets(searchByIdUrl, 'byId');
	} else {

	    if (validate(count, startTimeValue, endTimeValue, searchType)) return;

		let searchByTimeUrl = `${mainUrl}?limit=${count}&type=${searchType}&startTimeStamp=${startTime}&endTimeStamp=${endTime}`;

		getAllAssets(searchByTimeUrl, 'byTime');
	}
}

// for converting local time to GMT
function getUTCTime(time, same, seconds) {
	let datetime = new Date(time);
	datetime.setHours(datetime.getHours() + 5);
	datetime.setMinutes(datetime.getMinutes() + 30);

	if (same) {
		datetime.setSeconds(seconds);
	}
	return Date.parse(datetime).toString().slice(0, -3);
}

// function for populating more info section
async function getMapInfo() {
	console.log(assetsCount.innerHTML);

	let assets,trucksCountValue=0,typeArr = [];

	await axios.get(mainUrl).then((body) => (assets = body.data.assets)).catch((err) => console.log(err));

	assetsCount.innerHTML = assets.length;

	assets.forEach((asset) => {
        if(asset.assetType.toLowerCase()=='truck') trucksCountValue++;

		let type = asset.assetType.toLowerCase();
		if (typeArr.indexOf(type) == -1) {
			typeArr.push(type);
			assetsTypes.innerHTML += `${type}, `;
		}
	});

    console.log(trucksCountValue);
    
    salesCount.innerHTML = assets.length - trucksCountValue;
    trucksCount.innerHTML = trucksCountValue;

	assetsTypes.innerHTML.slice(0, -2);
}

// getMapInfo();

// validations
function validate(count, startTimeValue, endTimeValue, searchType) {
	if (startTimeValue > endTimeValue) {
		showErrorMsg(errors[2],'start time cannot be past end time filter.',endTimeFilter);

		return true;
	}

	let startTimeCheck = parseInt(Date.parse(startTimeValue).toString().slice(0, -3));
	let currentTimeCheck = parseInt(Date.now().toString().slice(0, -3));

	if (searchType != 'SALESPERSON' && searchType != 'TRUCK' && searchType != '') {
		
		showErrorMsg(errors[0],"invalid asset type",searchType);
		return true;
	}

	if (count <= 0) {
		showErrorMsg(errors[1],'no. of assets cannot be less than 0',assetsCount);
		return true;
	}

	return false;
}

function showErrorMsg(errorElement,msg,inputElement){

	errorElement.innerHTML= msg;

	errorElement.style.opacity = 1;

	inputElement.addEventListener('focus',()=>{
		errorElement.style.opacity=0;
	})
}