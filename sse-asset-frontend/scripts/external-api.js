const resource = "https://jumbogps-geo.anugrahsinghal.repl.co/assets";

const jwtToken =
  "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhbnUiLCJzY29wZXMiOlsiUk9MRV9BRE1JTiJdLCJpYXQiOjE2MTYwODcxNjYsImV4cCI6MTYxODA4NzE2Nn0.xTM2kH7HPx5GpoGbtpftOkg3iStjhSjkn77CPn5Q5LR3SjP5-4nbxRL4HPynEauInM49OvJlyvNAspyWy_FhgQ";

function getAssetData(limit) {
  console.log(arguments);

  if (limit === undefined || limit <= 0 || isNaN(limit)) {
    limit = 100;
    triggerIframe("Invalid Limit Entered. Using default value 100");
  }

  let url = new URL(resource);

  let params = {
    limit: limit,
  };
  url.search = new URLSearchParams(params).toString();

  console.log(url);

  makeFetchCallAndShowAssetData(url);
}

function getAssetDataWithFilters(
  limit,
  typeFilter,
  startDateFilter,
  endDateFilter
) {
  // call the API
  console.log(arguments);

  if (limit === undefined || limit <= 0 || isNaN(limit)) {
    limit = 100;
    triggerIframe("Invalid Limit Entered. Using default value 100");
  }

  let url = new URL(resource);

  let params = {
    limit: limit,
  };
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

  console.log(url);

  makeFetchCallAndShowAssetData(url, false);
}

function getAssetById(assetId) {
  // call the API
  let url = new URL(`${resource}/${assetId}`);

  console.log(url);

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
      console.log(response.body);
      console.log(response);
      // handle response code
      if (response.ok) {
        console.log("Data Loaded Successfully");
        return response.json();
      } else {
        throw new Error(response.status + "");
      }
    })
    .then((data) => {
      console.log(data);
      // if all ok parse data and return from there only
      if (true === isSingleObject) {
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
        triggerIframe("Unauthorized");
      } else if (error.message === "400") {
        triggerIframe("Invalid Parameters Provided");
      } else if (error.message === "404") {
        triggerIframe("No Asset Found For Given Id");
      } else {
        triggerIframe("Something Went Wrong. Please contact Support Team.");
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
      console.log(response.body);
      console.log(response);
      // handle response code
      if (response.ok) {
        console.log("Data Loaded Successfully");
        return response.json();
      } else {
        throw new Error(response.status + "");
      }
    })
    .then((data) => {
      console.log(data);
      // if all ok parse data and return from there only
      showTimeLineView(data);
    })
    .catch((error) => {
      // else if error occurred catch and show alter(<correct message according to response status>)
      console.error("Error while Loading data:", error);
			localStorage.removeItem("current-asset-id");
      if (error.message === "403") {
        triggerIframe("Unauthorized");
      } else if (error.message === "400") {
        triggerIframe("Invalid Parameters Provided");
      } else if (error.message === "404") {
        triggerIframe("No Asset Found For Given Id");
      } else {
        triggerIframe("Something Went Wrong. Please contact Support Team.");
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

function loadMemes() {
  fetch(encodeURI(xmemeBackendUrl))
    .then((response) => {
      log(response.body);
      log(response);
      if (response.ok) {
        console.log("Memes Loaded Successfully");
        return response.json();
      } else {
        throw new Error(response.status + "");
      }
    })
    .then((data) => {
      log("Total Data Received = z" + data.length);
      let cards = template({ memes: data });
      // log(cards);
      document.querySelector(".meme-post-wrapper").innerHTML = cards;
    })
    .then(() => {
      log("rendering complete");
      attachButtonsToTheLoadedMemes();
      log("attached buttons");
      setThemeForWindow();
    })
    .catch((error) => {
      console.error("Error while Loading data:", error);
      if (error.message === "403") {
        triggerIframe("Unauthorized");
      } else if (error.message === "400") {
        triggerIframe("Invalid Parameters Provided");
      } else {
        triggerIframe("Something Went Wrong. Please contact Support Team.");
      }
    });
}

/**
 * This function is triggerd on create/edit/delete of the meme
 * It recieves message from the functions and display that message as a notification
 * It also functions to reload the page after a delay of 2 seconds so that user can read notification
 * after which it again goes back to hidden state
 * @param {*} message The Message to be displayed in the Notification Frame
 * @param {*} reload - reloads full page on true or undefined | does not reload on false
 */
function triggerIframe(message) {
  alert(message);
  //   console.log("IFRAME TRIGGER " + message);
  //   let notification = document.querySelector("#meme-notification");
  //   let notificationMsg = document.querySelector("#meme-notification-message");
  //   notificationMsg.innerText = message + "[Page will reload]";

  //   notification.style.display = "unset";

  //   console.log("END IFRAME TRIGGER " + message);

  //   setTimeout(() => {
  //     console.log("Hide I frame start");
  //     let notification = document.querySelector("#meme-notification");
  //     notification.style.display = "none";
  //     console.log("Hide I frame complete");
  //     if (reload === true || reload === undefined) {
  //       location.reload();
  //     }
  //   }, 2000);
}
