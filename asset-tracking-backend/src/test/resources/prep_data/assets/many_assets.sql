-- outliers
insert into asset (asset_type, description, geofence, last_reported_coordinates, last_reported_timestamp, route, title, id)
values ('TRUCK', '', null, 'POINT (80.911597 26.828994)', '2021-03-24T20:48:07', null, '', nextval('asset_id_sequence'));
insert into asset (asset_type, description, geofence, last_reported_coordinates, last_reported_timestamp, route, title, id)
values ('TRUCK', '', null, 'POINT (80.911597 26.828994)', '2021-03-24T21:48:07', null, '', nextval('asset_id_sequence'));
-- 1 hour time slot
insert into asset (asset_type, description, geofence, last_reported_coordinates, last_reported_timestamp, route, title, id)
values ('TRUCK', '', null, 'POINT (80.911597 26.828994)', '2021-03-24T22:40:07', null, '', nextval('asset_id_sequence'));
insert into asset (asset_type, description, geofence, last_reported_coordinates, last_reported_timestamp, route, title, id)
values ('TRUCK', '', null, 'POINT (80.911597 26.828994)', '2021-03-24T22:41:07', null, '', nextval('asset_id_sequence'));
insert into asset (asset_type, description, geofence, last_reported_coordinates, last_reported_timestamp, route, title, id)
values ('SALESPERSON', '', null, 'POINT (80.911597 26.828994)', '2021-03-24T22:42:07', null, '', nextval('asset_id_sequence'));
insert into asset (asset_type, description, geofence, last_reported_coordinates, last_reported_timestamp, route, title, id)
values ('SALESPERSON', '', null, 'POINT (80.911597 26.828994)', '2021-03-24T22:43:07', null, '', nextval('asset_id_sequence'));
insert into asset (asset_type, description, geofence, last_reported_coordinates, last_reported_timestamp, route, title, id)
values ('SALESPERSON', '', null, 'POINT (80.911597 26.828994)', '2021-03-24T22:44:07', null, '', nextval('asset_id_sequence'));
insert into asset (asset_type, description, geofence, last_reported_coordinates, last_reported_timestamp, route, title, id)
values ('TRUCK', '', null, 'POINT (80.911597 26.828994)', '2021-03-24T22:45:07', null, '', nextval('asset_id_sequence'));
insert into asset (asset_type, description, geofence, last_reported_coordinates, last_reported_timestamp, route, title, id)
values ('TRUCK', '', null, 'POINT (80.911597 26.828994)', '2021-03-24T22:46:07', null, '', nextval('asset_id_sequence'));
insert into asset (asset_type, description, geofence, last_reported_coordinates, last_reported_timestamp, route, title, id)
values ('TRUCK', '', null, 'POINT (80.911597 26.828994)', '2021-03-24T22:47:07', null, '', nextval('asset_id_sequence'));
insert into asset (asset_type, description, geofence, last_reported_coordinates, last_reported_timestamp, route, title, id)
values ('TRUCK', '', null, 'POINT (80.911597 26.828994)', '2021-03-24T22:48:07', null, '', nextval('asset_id_sequence'));

-- -- outlier data
-- insert into location_data (asset_id, coordinates, timestamp, id)
-- values (3000, 'POINT (80.911597 26.828994)', '2021-03-23T21:48:07', nextval('location_data_id_sequence'));
-- -- data within 24 hrs
-- insert into location_data (asset_id, coordinates, timestamp, id)
-- values (3000, 'POINT (80.911597 26.828994)', '2021-03-24T21:48:07', nextval('location_data_id_sequence'));
-- insert into location_data (asset_id, coordinates, timestamp, id)
-- values (3000, 'POINT (80.911597 26.828994)', '2021-03-24T22:08:07', nextval('location_data_id_sequence'));
-- insert into location_data (asset_id, coordinates, timestamp, id)
-- values (3000, 'POINT (80.911597 26.828994)', '2021-03-24T22:18:07', nextval('location_data_id_sequence'));
-- insert into location_data (asset_id, coordinates, timestamp, id)
-- values (3000, 'POINT (80.911597 26.828994)', '2021-03-24T22:28:07', nextval('location_data_id_sequence'));
-- insert into location_data (asset_id, coordinates, timestamp, id)
-- values (3000, 'POINT (80.911597 26.828994)', '2021-03-24T22:38:07', nextval('location_data_id_sequence'));
-- insert into location_data (asset_id, coordinates, timestamp, id)
-- values (3000, 'POINT (80.911597 26.828994)', '2021-03-24T22:40:07', nextval('location_data_id_sequence'));
-- insert into location_data (asset_id, coordinates, timestamp, id)
-- values (3000, 'POINT (80.911597 26.828994)', '2021-03-24T22:41:07', nextval('location_data_id_sequence'));
-- insert into location_data (asset_id, coordinates, timestamp, id)
-- values (3000, 'POINT (80.911597 26.828994)', '2021-03-24T22:42:07', nextval('location_data_id_sequence'));
-- insert into location_data (asset_id, coordinates, timestamp, id)
-- values (3000, 'POINT (80.911597 26.828994)', '2021-03-24T22:43:07', nextval('location_data_id_sequence'));
-- insert into location_data (asset_id, coordinates, timestamp, id)
-- values (3000, 'POINT (80.911597 26.828994)', '2021-03-24T22:44:07', nextval('location_data_id_sequence'));
-- insert into location_data (asset_id, coordinates, timestamp, id)
-- values (3000, 'POINT (80.911597 26.828994)', '2021-03-24T22:45:07', nextval('location_data_id_sequence'));
-- insert into location_data (asset_id, coordinates, timestamp, id)
-- values (3000, 'POINT (80.911597 26.828994)', '2021-03-24T22:46:07', nextval('location_data_id_sequence'));
-- insert into location_data (asset_id, coordinates, timestamp, id)
-- values (3000, 'POINT (80.911597 26.828994)', '2021-03-24T22:47:07', nextval('location_data_id_sequence'));
-- insert into location_data (asset_id, coordinates, timestamp, id)
-- values (3000, 'POINT (80.911597 26.828994)', '2021-03-24T22:48:07', nextval('location_data_id_sequence'));