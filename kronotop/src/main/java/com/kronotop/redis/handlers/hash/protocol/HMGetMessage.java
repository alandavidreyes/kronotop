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

package com.kronotop.redis.handlers.hash.protocol;

import com.kronotop.server.ProtocolMessage;
import com.kronotop.server.Request;

import java.util.ArrayList;
import java.util.List;

public class HMGetMessage implements ProtocolMessage<String> {
    public static final String COMMAND = "HMGET";
    public static final int MINIMUM_PARAMETER_COUNT = 3;
    private final Request request;
    private final List<String> fields = new ArrayList<>();
    private String key;

    public HMGetMessage(Request request) {
        this.request = request;
        parse();
    }

    private void parse() {
        byte[] rawKey = new byte[request.getParams().get(0).readableBytes()];
        request.getParams().get(0).readBytes(rawKey);
        key = new String(rawKey);

        for (int i = 1; i < request.getParams().size(); i++) {
            byte[] rawField = new byte[request.getParams().get(i).readableBytes()];
            request.getParams().get(i).readBytes(rawField);
            String field = new String(rawField);
            fields.add(field);
        }
    }

    public List<String> getFields() {
        return fields;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public List<String> getKeys() {
        return null;
    }
}
