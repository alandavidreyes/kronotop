/*
 * Copyright (c) 2023-2025 Kronotop
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

package com.kronotop.volume.replication;

import com.apple.foundationdb.tuple.Versionstamp;
import com.kronotop.Context;
import com.kronotop.cluster.RoutingEventHook;
import com.kronotop.cluster.sharding.ShardKind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StopReplicationHook extends BaseRoutingEventHook implements RoutingEventHook {
    private static final Logger LOGGER = LoggerFactory.getLogger(StopReplicationHook.class);

    public StopReplicationHook(Context context) {
        super(context);
    }

    @Override
    public void run(ShardKind shardKind, int shardId) {
        Versionstamp slotId = findSlotId(shardKind, shardId);
        if (slotId == null) {
            LOGGER.warn("No ReplicationSlot found for ShardKind: {} ShardId: {}", shardKind, shardId);
            return;
        }
        ReplicationService replicationService = context.getService(ReplicationService.NAME);
        replicationService.stopReplication(slotId);
    }
}
