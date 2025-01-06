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

package com.kronotop.cluster.handlers;

import com.apple.foundationdb.Transaction;
import com.apple.foundationdb.directory.DirectorySubspace;
import com.kronotop.Context;
import com.kronotop.VersionstampUtils;
import com.kronotop.cluster.*;
import com.kronotop.cluster.sharding.ShardKind;
import com.kronotop.cluster.sharding.ShardStatus;
import com.kronotop.common.KronotopException;
import com.kronotop.server.resp3.ArrayRedisMessage;
import com.kronotop.server.resp3.IntegerRedisMessage;
import com.kronotop.server.resp3.RedisMessage;
import com.kronotop.server.resp3.SimpleStringRedisMessage;
import com.kronotop.volume.VolumeConfigGenerator;
import io.netty.buffer.ByteBuf;

import java.util.*;

public class BaseKrAdminSubcommandHandler {
    protected final Context context;
    protected final MembershipService membership;
    protected final RoutingService routing;

    public BaseKrAdminSubcommandHandler(RoutingService service) {
        this.context = service.getContext();
        this.routing = service;
        // Membership service has been started before RoutingService.
        this.membership = service.getContext().getService(MembershipService.NAME);
    }

    /**
     * Describes the metadata and status of a specific shard.
     * Retrieves information about the primary member, standby members,
     * synchronized standby members, and the status of the shard based on its type and ID.
     *
     * @param tr        The transaction object used for accessing the database.
     * @param shardKind The kind of shard, represented as a {@link ShardKind}.
     * @param shardId   The ID of the shard to describe.
     * @return A map where the keys are {@link RedisMessage} instances representing metadata
     * descriptors (e.g., "primary", "standbys", "sync_standbys", "status") and the
     * values are {@link RedisMessage} instances containing the respective information.
     */
    protected Map<RedisMessage, RedisMessage> describeShard(Transaction tr, ShardKind shardKind, int shardId) {
        DirectorySubspace shardSubspace = context.getDirectorySubspaceCache().get(shardKind, shardId);
        Map<RedisMessage, RedisMessage> shard = new LinkedHashMap<>();

        String primaryRouteMemberId = MembershipUtils.loadPrimaryMemberId(tr, shardSubspace);
        if (primaryRouteMemberId == null) {
            primaryRouteMemberId = "";
        }
        shard.put(new SimpleStringRedisMessage("primary"), new SimpleStringRedisMessage(primaryRouteMemberId));

        List<RedisMessage> standbyMessages = new ArrayList<>();
        Set<String> standbys = MembershipUtils.loadStandbyMemberIds(tr, shardSubspace);
        if (standbys != null) {
            for (String standby : standbys) {
                standbyMessages.add(new SimpleStringRedisMessage(standby));
            }
        }
        shard.put(new SimpleStringRedisMessage("standbys"), new ArrayRedisMessage(standbyMessages));

        List<RedisMessage> syncStandbyMessages = new ArrayList<>();
        Set<String> syncStandbys = MembershipUtils.loadSyncStandbyMemberIds(tr, shardSubspace);
        if (standbys != null) {
            for (String syncStandby : syncStandbys) {
                syncStandbyMessages.add(new SimpleStringRedisMessage(syncStandby));
            }
        }
        shard.put(new SimpleStringRedisMessage("sync_standbys"), new ArrayRedisMessage(syncStandbyMessages));

        ShardStatus status = ShardUtils.getShardStatus(tr, shardSubspace);
        shard.put(new SimpleStringRedisMessage("status"), new SimpleStringRedisMessage(status.name()));

        // TODO: This is not sufficient but it works for now.
        // Ideally, we should get this info from FDB, re-generating the volume name is not good.
        List<RedisMessage> linkedVolumes = new ArrayList<>();
        linkedVolumes.add(new SimpleStringRedisMessage(VolumeConfigGenerator.volumeName(shardKind, shardId)));
        shard.put(new SimpleStringRedisMessage("linked_volumes"), new ArrayRedisMessage(linkedVolumes));

        return shard;
    }

    /**
     * Retrieves the number of shards for a given shard kind.
     *
     * @param kind The type of shard whose number is to be retrieved. Must be of type {@link ShardKind}.
     * @return The number of shards for the specified shard kind.
     * @throws IllegalArgumentException if the specified shard kind is not recognized.
     */
    protected int getNumberOfShards(ShardKind kind) {
        if (kind.equals(ShardKind.REDIS)) {
            return membership.getContext().getConfig().getInt("redis.shards");
        }
        throw new IllegalArgumentException("Unknown shard kind: " + kind);
    }

    /**
     * Reads the shard status from the provided ByteBuf and converts it into a ShardStatus enum value.
     *
     * @param shardStatusBuf the ByteBuf containing the raw bytes of the shard status
     * @return the corresponding ShardStatus enum value
     * @throws KronotopException if the shard status is invalid
     */
    protected ShardStatus readShardStatus(ByteBuf shardStatusBuf) {
        byte[] rawShardStatus = new byte[shardStatusBuf.readableBytes()];
        shardStatusBuf.readBytes(rawShardStatus);
        String stringShardStatus = new String(rawShardStatus);

        try {
            return ShardStatus.valueOf(stringShardStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new KronotopException("Invalid shard status " + stringShardStatus);
        }
    }

    /**
     * Reads the member status from the provided ByteBuf.
     *
     * @param memberStatusBuf the ByteBuf containing the raw bytes of the member status
     * @return the corresponding MemberStatus enum value
     * @throws KronotopException if the member status is invalid
     */
    protected MemberStatus readMemberStatus(ByteBuf memberStatusBuf) {
        byte[] rawMemberStatus = new byte[memberStatusBuf.readableBytes()];
        memberStatusBuf.readBytes(rawMemberStatus);
        String stringMemberStatus = new String(rawMemberStatus);
        try {
            return MemberStatus.valueOf(stringMemberStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new KronotopException("Invalid member status " + stringMemberStatus);
        }
    }

    /**
     * Reads the shard kind from the provided ByteBuf.
     *
     * @param shardKindBuf the ByteBuf containing the raw bytes of the shard kind
     * @return the corresponding ShardKind enum value
     * @throws KronotopException if the shard kind is invalid
     */
    protected ShardKind readShardKind(ByteBuf shardKindBuf) {
        String rawKind = readAsString(shardKindBuf);
        try {
            return ShardKind.valueOf(rawKind.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new KronotopException("invalid shard kind");
        }
    }

    /**
     * Reads the shard ID from the provided ByteBuf and validates it based on the given shard kind.
     *
     * @param shardKind  The type of shard whose ID is being read. Must be of type {@link ShardKind}.
     * @param shardIdBuf The ByteBuf containing the raw bytes of the shard ID.
     * @return The validated shard ID as an integer.
     * @throws InvalidShardIdException if the shard ID is not a valid integer or is out of bounds for the specified shard kind.
     */
    protected int readShardId(ShardKind shardKind, ByteBuf shardIdBuf) {
        String rawShardId = readAsString(shardIdBuf);
        return readShardId(shardKind, rawShardId);
    }

    /**
     * Reads and validates the shard ID based on the provided shard kind and raw shard ID string.
     *
     * @param shardKind  The type of shard whose ID is being read. Must be of type {@link ShardKind}.
     * @param rawShardId A string representation of the shard ID.
     * @return The validated shard ID as an integer.
     * @throws InvalidShardIdException if the shard ID is not a valid integer or is out of bounds for the specified shard kind.
     */
    protected int readShardId(ShardKind shardKind, String rawShardId) {
        try {
            int shardId = Integer.parseInt(rawShardId);
            // Validate
            if (shardId < 0 || shardId > getNumberOfShards(shardKind) - 1) {
                throw new InvalidShardIdException();
            }
            return shardId;
        } catch (NumberFormatException e) {
            throw new InvalidShardIdException();
        }
    }

    /**
     * Reads and validates a member ID from the provided ByteBuf.
     * If the member ID length is 4, it attempts to resolve the full ID by finding a matching member prefix.
     * If the member ID length is not 4, it validates the ID.
     *
     * @param memberIdBuf the ByteBuf containing the raw bytes of the member ID to be read
     * @return the resolved or validated member ID as a string
     * @throws KronotopException if the member ID is invalid or no member can be resolved with the prefix
     */
    protected String readMemberId(ByteBuf memberIdBuf) {
        String memberId = readAsString(memberIdBuf);
        if (memberId.length() == 4) {
            Member member = findMemberWithPrefix(memberId);
            return member.getId();
        }
        // Validate the member id.
        if (MemberIdGenerator.validateId(memberId)) {
            return memberId;
        } else {
            throw new KronotopException("Invalid memberId: " + memberId);
        }
    }

    /**
     * Reads the content of the provided ByteBuf as a string.
     *
     * @param buf the ByteBuf containing the raw bytes to be read
     * @return a string representation of the bytes in the provided ByteBuf
     */
    protected String readAsString(ByteBuf buf) {
        byte[] raw = new byte[buf.readableBytes()];
        buf.readBytes(raw);
        return new String(raw);
    }

    /**
     * Converts a given Member object into a Map of RedisMessage key-value pairs.
     *
     * @param member the Member object to be converted
     * @return a Map containing RedisMessage key-value pairs representing the attributes of the Member object
     */
    protected Map<RedisMessage, RedisMessage> memberToRedisMessage(Member member) {
        Map<RedisMessage, RedisMessage> current = new LinkedHashMap<>();

        current.put(new SimpleStringRedisMessage("status"), new SimpleStringRedisMessage(member.getStatus().toString()));

        String processId = VersionstampUtils.base64Encode(member.getProcessId());
        current.put(new SimpleStringRedisMessage("process_id"), new SimpleStringRedisMessage(processId));

        current.put(new SimpleStringRedisMessage("external_host"), new SimpleStringRedisMessage(member.getExternalAddress().getHost()));
        current.put(new SimpleStringRedisMessage("external_port"), new IntegerRedisMessage(member.getExternalAddress().getPort()));

        current.put(new SimpleStringRedisMessage("internal_host"), new SimpleStringRedisMessage(member.getInternalAddress().getHost()));
        current.put(new SimpleStringRedisMessage("internal_port"), new IntegerRedisMessage(member.getInternalAddress().getPort()));

        long latestHeartbeat = membership.getLatestHeartbeat(member);
        current.put(new SimpleStringRedisMessage("latest_heartbeat"), new IntegerRedisMessage(latestHeartbeat));

        return current;
    }

    /**
     * Finds a member with the given prefix in their ID.
     *
     * @param prefix the prefix to search for in member IDs
     * @return the member whose ID starts with the given prefix
     * @throws KronotopException if no member or more than one member is found with the given prefix
     */
    protected Member findMemberWithPrefix(String prefix) {
        Set<Member> result = new HashSet<>();
        TreeSet<Member> members = membership.listMembers();
        for (Member member : members) {
            if (member.getId().startsWith(prefix)) {
                result.add(member);
            }
        }
        if (result.isEmpty()) {
            throw new KronotopException("no member found with prefix: " + prefix);
        }
        if (result.size() > 1) {
            throw new KronotopException("more than one member found with prefix: " + prefix);
        }
        return result.iterator().next();
    }
}
