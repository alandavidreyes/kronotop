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

package com.kronotop.cluster.handlers;

import com.apple.foundationdb.Transaction;
import com.kronotop.cluster.MembershipService;
import com.kronotop.redis.server.SubcommandHandler;
import com.kronotop.server.Request;
import com.kronotop.server.Response;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;

class RemoveMemberSubcommand extends BaseKrAdminSubcommandHandler implements SubcommandHandler {

    RemoveMemberSubcommand(MembershipService service) {
        super(service);
    }

    @Override
    public void execute(Request request, Response response) {
        RemoveMemberParameters parameters = new RemoveMemberParameters(request.getParams());
        try (Transaction tr = context.getFoundationDB().createTransaction()) {
            membership.removeMember(tr, parameters.memberId);
            membership.triggerRoutingEventsWatcher(tr);
        }
        response.writeOK();
    }

    private class RemoveMemberParameters {
        private final String memberId;

        RemoveMemberParameters(ArrayList<ByteBuf> params) {
            memberId = readMemberId(params.get(1));
        }
    }
}
