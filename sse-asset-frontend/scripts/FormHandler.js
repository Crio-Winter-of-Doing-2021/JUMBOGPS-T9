const form = document.getElementsByTagName("form")[0];
const searchFilter = document.getElementsByName("criteria")[0];
const startTimeFilter = document.getElementsByName("startTime")[0];
const endTimeFilter = document.getElementsByName("endTime")[0];
const assetsCount = document.getElementsByName("maxAssetsCount")[0];
const errors = document.getElementsByClassName("error");
const inputLabels = document.getElementsByTagName("label");
const logoutBtn = document.getElementById("logoutBtn");
const resetBtn = document.getElementById("resetBtn");

form.addEventListener("submit", handleSubmit);

// function to handle form submit
function handleSubmit(e) {
  e.preventDefault();

  let searchType = searchFilter.value.toUpperCase().trim();
  let searchId = parseInt(searchType);
  let startTimeValue = startTimeFilter.value;
  let endTimeValue = endTimeFilter.value;
  let count = assetsCount.value;

  validate(count, startTimeValue, endTimeValue, searchType);

  let startTime =
    startTimeValue === endTimeValue
      ? getUTCTime(startTimeValue, true, 0)
      : getUTCTime(startTimeValue, false, 0);

  let endTime =
    startTimeValue === endTimeValue
      ? getUTCTime(endTimeValue, true, 59)
      : getUTCTime(endTimeValue, false, 0);

  if (Number.isInteger(searchId)) {
    // let searchByIdUrl = `${mainUrl}/${searchId}`;

    getAssetById(searchId);
  } else {
    if (validate(count, startTimeValue, endTimeValue, searchType)) return;

    // let searchByTimeUrl = `${mainUrl}?limit=${count}&type=${searchType}&startTimeStamp=${startTime}&endTimeStamp=${endTime}`;

    // getAllAssets(searchByTimeUrl, "byTime");
    getAssetDataWithFilters(count, searchType, startTimeValue, endTimeValue);
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

// validations
function validate(count, startTimeValue, endTimeValue, searchType) {
  if (startTimeValue > endTimeValue) {
    showErrorMsg(
      errors[2],
      "start time cannot be past end time filter.",
      endTimeFilter
    );

    return true;
  }

  let startTimeCheck = parseInt(
    Date.parse(startTimeValue).toString().slice(0, -3)
  );
  let currentTimeCheck = parseInt(Date.now().toString().slice(0, -3));

  if (
    !Number.isInteger(parseInt(searchType)) &&
    searchType != "SALESPERSON" &&
    searchType != "TRUCK" &&
    searchType != ""
  ) {
    showErrorMsg(errors[0], "invalid asset type", searchFilter);
    return true;
  }

  if (count != "" && count < 0) {
    showErrorMsg(errors[1], "count cannot be less than 0", assetsCount);

    return true;
  }

  return false;
}

function showErrorMsg(errorElement, msg, inputElement) {
  errorElement.innerHTML = msg;

  errorElement.style.opacity = 1;

  inputElement.addEventListener("focus", () => {
    errorElement.style.opacity = 0;
  });
}

function disbaleTimeInput() {
  let value = parseInt(searchFilter.value.toUpperCase().trim());

  console.log(value);

  if (value != null && Number.isInteger(value)) {
    startTimeFilter.disabled = true;
    endTimeFilter.disabled = true;
    assetsCount.disabled = true;

    for (let x = 0; x < inputLabels.length; x++) {
      inputLabels[x].style.opacity = 0;
    }
  } else {
    startTimeFilter.disabled = false;
    endTimeFilter.disabled = false;
    assetsCount.disabled = false;

    for (let x = 0; x < inputLabels.length; x++) {
      inputLabels[x].style.opacity = 1;
    }
  }
}

function handleLogout() {
  localStorage.removeItem("token");
  window.location.assign("login.html");
}

function clearForm() {
  searchFilter.value = "";
  assetsCount.value = "100";
  startTimeFilter.value = "";
  endTimeFilter.value = "";
  errors.value = ""
}

function handleReset() {
	clearForm()
  getAssetData(assetsCount.value)
}

searchFilter.onkeypress = disbaleTimeInput;
searchFilter.onchange = disbaleTimeInput;
logoutBtn.onclick = handleLogout;
resetBtn.onclick = handleReset;
