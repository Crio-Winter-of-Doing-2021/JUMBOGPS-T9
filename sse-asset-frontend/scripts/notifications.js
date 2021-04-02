const resourceUrl =
  "https://jumbogps-geo.anugrahsinghal.repl.co/api/assets/subscribe";

// const resourceUrl = "http://localhost:8080/api/assets/subscribe";

function getNotificationHeader(eventType) {
  let header = "";
  console.log(eventType);
  if (eventType === "route-deviation") {
    header = "Route Deviation";
  } else if (eventType === "geofence-exit") {
    header = "Outside Geofence";
  } else {
    header = "Something went wrong";
  }

  return header;
}

function showNotification(eventData) {
  console.group("showNotification");

  let notificationData = eventData[1].data;
  console.log(notificationData);

  const header = getNotificationHeader(notificationData.eventType);
  const message = notificationData.message;

  const notification = new Notification(header, {
    body: message,
    icon: "./exclamation-mark.png",
  });
  console.groupEnd();
}

function subscribeToSSE() {
  console.group("Event Subscription");
  // const eventSource = new EventSourcePolyfill(resourceUrl);
  const eventSource = new EventSource(resourceUrl);

  console.log("subscribed");

  eventSource.onopen = (event) => {
    console.log("connection opened");
    console.log(event);
  };

  console.log("open");

  eventSource.onmessage = (result) => {
    console.log("Message Received");
    // console.log(JSON.stringify(result));
    const data = JSON.parse(result.data);
    console.log("Data: ", data);
    showNotification(data);
  };
  console.log("message");

  eventSource.onerror = (err) => {
    console.error("EventSource error: ", err);
    eventSource.close();
  };
  console.log("error");

  console.groupEnd();
}

console.log("Notification Permission => " + Notification.permission);

if (Notification.permission === "granted") {
  subscribeToSSE();
} else if (Notification.permission !== "denied") {
  Notification.requestPermission().then((permission) => {
    if (permission === "granted") {
      subscribeToSSE();
    }
  });
}
