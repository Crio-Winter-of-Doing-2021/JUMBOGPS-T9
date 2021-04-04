const resource = "https://jumbogps-main.anugrahsinghal.repl.co/assets";

const jwtToken =
  "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhbnUiLCJzY29wZXMiOlsiUk9MRV9BRE1JTiJdLCJpYXQiOjE2MTYwODcxNjYsImV4cCI6MTYxODA4NzE2Nn0.xTM2kH7HPx5GpoGbtpftOkg3iStjhSjkn77CPn5Q5LR3SjP5-4nbxRL4HPynEauInM49OvJlyvNAspyWy_FhgQ";

function getAssetData(limit) {

  let url = new URL(resource);

  let params = {limit};

  url.search = new URLSearchParams(params).toString();

  makeFetchCallAndShowAssetData(url);
}

function getAssetDataWithFilters(limit,typeFilter,startDateFilter,endDateFilter) {
 
  let url = new URL(resource);

  let params = {limit};

  // or: let params = [["lat", "35.696233"],["long", "139.570431"],];

  if (typeFilter !== undefined && typeFilter !== "") {
    params["type"] = typeFilter;
  }
  if (
    startDateFilter !== undefined &&
    startDateFilter !== "" &&
    endDateFilter !== undefined &&
    endDateFilter !== ""
  ) {
    params["startTimeStamp"] = startDateFilter;
    params["endTimeStamp"] = endDateFilter;
  }

  url.search = new URLSearchParams(params).toString();

  makeFetchCallAndShowAssetData(url, false);
}

function getAssetById(assetId) {
  // call the API
  let url = new URL(`${resource}/${assetId}`);

  makeFetchCallAndShowAssetData(url, true);
}

function makeFetchCallAndShowAssetData(url, isSingleObject) {
  fetch(encodeURI(url), {
    method: "GET", // *GET, POST, PUT, DELETE, etc.
    headers: {
      Authorization: `Bearer ${jwtToken}`,
    },
  })
    .then((response) => {
      // handle response code
      if (response.ok) {
        console.log("Data Loaded Successfully");
        return response.json();
      } else {
        throw new Error(response.status + "");
      }
    })
    .then((data) => {
      // if all ok parse data and return from there only
      if (isSingleObject) {
        let modifiedSingleAsset = {
          centroid: data.lastReportedCoordinates,
          assets: [data],
        };
        showAssetView(modifiedSingleAsset);
      } else {
        showAssetView(data);
      }
    })
    .catch((error) => {
      // else if error occurred catch and show alter(<correct message according to response status>)
      console.error("Error while Loading data:", error);
      if (error.message === "403") {
        showPopupNotification("Unauthorized");
      } else if (error.message === "400") {
        showPopupNotification("Invalid Parameters Provided");
      } else if (error.message === "404") {
        showPopupNotification("No Asset Found For Given Id");
      } else {
        showPopupNotification("Something Went Wrong. Please contact Support Team.");
      }
    });
}

function getHistoryData(assetId) {
  let url = new URL(`${resource}/${assetId}/history`);

  localStorage.setItem("current-asset-id", assetId);

  fetch(encodeURI(url), {
    method: "GET", // *GET, POST, PUT, DELETE, etc.
    headers: {
      Authorization: `Bearer ${jwtToken}`,
    },
  })
    .then((response) => {
      // handle response code
      if (response.ok) {
        console.log("Data Loaded Successfully");
        return response.json();
      } else {
        throw new Error(response.status + "");
      }
    })
    .then((data) => {
      // if all ok parse data and return from there only
      showTimeLineView(data);
    })
    .catch((error) => {
      // else if error occurred catch and show alter(<correct message according to response status>)
      console.error("Error while Loading data:", error);
      localStorage.removeItem("current-asset-id");
      if (error.message === "403") {
        showPopupNotification("Unauthorized");
      } else if (error.message === "400") {
        showPopupNotification("Invalid Parameters Provided");
      } else if (error.message === "404") {
        showPopupNotification("No Asset Found For Given Id");
      } else {
        showPopupNotification("Something Went Wrong. Please contact Support Team.");
      }
    });
}

function authFetch() {
  return fetch("https://jumbogps-geo.anugrahsinghal.repl.co/assets?limit=", {
    method: "GET", // *GET, POST, PUT, DELETE, etc.
    headers: {
      Authorization: `Bearer ${jwtToken}`,
    },
  })
    .then((response) => response.json())
    .then((data) => {
      console.log(data);

      let centroid = data.centroid;
      let assetAsFeatures = getAllFeaturesFromAssets(data);

      let geoJson = {
        type: "FeatureCollection",
        features: assetAsFeatures,
      };

      renderMapForAssetViewData(centroid, geoJson);
    })
    .catch((err) => {
      console.log("err");
    });
}

/**
 * This function is triggerd for notifications
 * It recieves message from the functions and display that message as a notification
 * hides after a delay of 5 seconds
 * @param {*} message The Message to be displayed in the Notification Frame
 */
function triggerIframe(message) {
  let notification = document.querySelector("#notification");
  let notificationMsg = document.querySelector("#notification-message");
  notificationMsg.innerText = message;

  notification.style.display = "unset";

  setTimeout(() => {
    console.log("Hide I frame start");
    let notification = document.querySelector("#notification");
    notification.style.display = "none";
    console.log("Hide I frame complete");
  }, 5000);
}
