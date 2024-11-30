/*
 * Copyright (c) 2023-2024 Kronotop
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

package com.kronotop.redis.handlers.connection;

import com.kronotop.commandbuilder.redis.RedisCommandBuilder;
import com.kronotop.redis.handlers.BaseHandlerTest;
import com.kronotop.server.Response;
import com.kronotop.server.resp3.SimpleStringRedisMessage;
import com.typesafe.config.Config;
import io.lettuce.core.codec.StringCodec;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.UnknownHostException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class AuthHandlerRequirePassTest extends BaseHandlerTest {
    @Override
    @BeforeEach
    public void setup() throws UnknownHostException, InterruptedException {
        Config config = loadConfig("auth-requirepass-test.conf");
        setupCommon(config);
    }

    @Test
    public void test_AuthOnlyWithPass() {
        EmbeddedChannel noauthChannel = kronotopInstance.newChannel();
        RedisCommandBuilder<String, String> cmd = new RedisCommandBuilder<>(StringCodec.ASCII);
        ByteBuf buf = Unpooled.buffer();
        cmd.auth("devpass").encode(buf);

        noauthChannel.writeInbound(buf);
        Object msg = noauthChannel.readOutbound();
        assertInstanceOf(SimpleStringRedisMessage.class, msg);
        SimpleStringRedisMessage actualMessage = (SimpleStringRedisMessage) msg;
        assertEquals(Response.OK, actualMessage.content());
    }
}
