/*
 * Copyright 2021 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License, version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.kronotop.server.resp3;

import io.netty.util.internal.UnstableApi;

/**
 * Header of Redis Map Message. the length represent the number of field-value pairs,
 * but the number of redis message.
 */
@UnstableApi
public final class MapHeaderRedisMessage extends AggregatedHeaderRedisMessage {

    /**
     * Creates a {@link MapHeaderRedisMessage} for the given {@code length}.
     *
     * @param length
     */
    public MapHeaderRedisMessage(long length) {
        super(length);
    }
}
