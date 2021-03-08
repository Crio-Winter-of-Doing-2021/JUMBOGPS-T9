![image](https://learn.crio.do/static/media/crio-head.440aa3b7.svg)  
[crio.do](https://learn.crio.do)

## Design Document

#### This document will be used to pen-down decisions that were made while creating the project. It can be used as a future reference and/or validate decisions.

---

### Using SQL vs NoSQL

1. The data we receive from the devices i.e. GPS data has a defined format.
1. Since the format of data is defined using SQL.
1. Some operations like getting historical data for asset will also be easily accomplished by using SQL queries, and we can maintain proper indexes on
   columns like timestamp for such queries.
    1. Using a time-series optimized DB structure would definitely be a better approach, but will take a longer time get used to due to
       non-familiarity. We can maybe take that up as a future enhancement.
1. A major reason for using SQL over NoSQL DB for this project is the familiarity with SQL.

We have chosen to use Postgres for this project.

---

### Data From Devices

2 approaches can be used for the time-stamp data of devices

1. Let the device/asset transmit the timestamp
1. Let the backend generate timestamp when it gets the location update from the Device

#### Scenario

An asset sends an update, but the network fails to deliver as asset is in transit and poor network conditions Asset keeps retrying and data transmits
after say 2 hours then the timestamp data is technically incorrect now  
BUT  
if asset timestamp is not generated at Backend. a malicious device can transmit wrong timestamps

After discussions with mentors and peers, it was decided that it was better to let the device transmit the timestamp.  
The security aspect can be taken care of on the asset/device itself to make it secure.

---

### Defining Asset Types

1. Pre-Defined Asset Types
1. Keep Asset Types Undefined, as text inputs from user/device

#### Pre-Defined Asset Types:

1. PROS
    1. Can Provide autocompletion/dropdown while searching/filtering for asset type, this makes for a better UX.
    1. DB queries can be optimized/indexed. Defining an ENUM in postgres and indexing the ENUM column will fetch results way faster than
       text-matching.
    1. Validations can be added on the backend to discard requests with wrong input values.
1. CONS:
    1. Asset can be of many types and as more and more assets are added that will also require back-end changes.

#### Keep Asset Types Undefined

1. PROS:
    1. Asset types can be dynamically increased with no intervention from the backend.
1. CONS:
    1. Slower Queries as data will be found using text-matching.
        1. Using indexes this might not be that big of a performance hit.
    1. To Provide Auto-complete functionality to the user for asset type based filter, an additional API call will be needed to fetch all possible
       input values for that type.

**Conclusion**  
After discussion with mentors Om and Nandan, I got conflicting answers.  
Nandan suggested going with option 2. Om suggested going with option 1.

I have decided to go with option 1 and keep my types defined.  
We can change it at a later point.

---

## Requirements

1. ### Support for time based filters which will take start date/time and end date/time as inputs.

   This requirement does not clearly state that if the time based filter is needed for a single asset or asset-map as a whole.  
   After discussions with mentors, it was decided that this requirement needs to be implemented for the map as a whole.    
   **Conclusion**  
   When filtered based on `start date/time and end date/time`, the backend will return assets whose latest location were updated between the given
   `time-range`.

1. ### Show a map on which markers show the latest locations of all assets.
   We are going to show the full view of the map with trackers placed over it. The user can zoom in on the area that they feel they need to explore.

---
Table

|   |   |   |   |   |
|---|---|---|---|---|
|   |   |   |   |   |

