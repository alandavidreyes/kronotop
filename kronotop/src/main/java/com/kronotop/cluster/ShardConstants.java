/*
 * Copyright (c) 2023-2025 Burak Sezer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kronotop.cluster;

public class ShardConstants {
    public static final byte ROUTE_PRIMARY_MEMBER_KEY = 0x01;
    public static final byte ROUTE_STANDBY_MEMBER_KEY = 0x02;
    public static final byte SHARD_STATUS_KEY = 0x03;
    public static final byte ROUTE_SYNC_STANDBY_MEMBERS = 0x04;
}
