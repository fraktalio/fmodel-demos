/*
 * Copyright (c) 2021 Fraktalio D.O.O. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

CREATE TABLE IF NOT EXISTS RestaurantR2DBCEntity
(
    id VARCHAR
(
    60
) DEFAULT RANDOM_UUID
(
) PRIMARY KEY,
    aggregate_version VARCHAR
(
    60
) NOT NULL,
    name VARCHAR
(
    60
) NOT NULL,
    status VARCHAR
(
    60
) NOT NULL,
    cuisine VARCHAR
(
    60
) NOT NULL,
    menu_status VARCHAR
(
    60
) NOT NULL,
    menu_id VARCHAR
(
    60
) NOT NULL,
    new_restaurant VARCHAR
(
    60
) NOT NULL
    );

CREATE TABLE IF NOT EXISTS MenuItemR2DBCEntity
(
    id VARCHAR
(
    60
) DEFAULT RANDOM_UUID
(
) PRIMARY KEY,
    menu_item_id VARCHAR
(
    60
) NOT NULL,
    restaurant_id VARCHAR NOT NULL,
    name VARCHAR
(
    60
) NOT NULL,
    price VARCHAR
(
    60
) NOT NULL,
    menu_id VARCHAR
(
    60
) NOT NULL,
    new_menu_item VARCHAR
(
    60
) NOT NULL
    );

CREATE TABLE IF NOT EXISTS RestaurantOrderR2DBCEntity
(
    id VARCHAR
(
    60
) DEFAULT RANDOM_UUID
(
) PRIMARY KEY,
    aggregate_version VARCHAR
(
    60
) NOT NULL,
    restaurant_id VARCHAR
(
    60
) NOT NULL,
    state VARCHAR
(
    60
) NOT NULL,
    new_restaurant_order VARCHAR
(
    60
) NOT NULL
    );

CREATE TABLE IF NOT EXISTS RestaurantOrderItemR2DBCEntity
(
    id VARCHAR
(
    60
) DEFAULT RANDOM_UUID
(
) PRIMARY KEY,
    order_id VARCHAR
(
    60
) NOT NULL,
    menu_item_id VARCHAR
(
    60
) NOT NULL,
    name VARCHAR
(
    60
) NOT NULL,
    quantity VARCHAR
(
    60
) NOT NULL,
    new_restaurant_order_item VARCHAR
(
    60
) NOT NULL
    );
