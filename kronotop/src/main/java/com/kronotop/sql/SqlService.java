/*
 * Copyright (c) 2023 Kronotop
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

package com.kronotop.sql;

import com.kronotop.core.CommandHandlerService;
import com.kronotop.core.Context;
import com.kronotop.core.KronotopService;
import com.kronotop.server.Handlers;
import com.kronotop.sql.backend.ddl.CreateSchema;
import com.kronotop.sql.backend.ddl.CreateTable;
import com.kronotop.sql.backend.ddl.DropSchema;
import com.kronotop.sql.backend.ddl.DropTable;
import com.kronotop.sql.backend.metadata.SqlMetadataService;
import org.apache.calcite.sql.SqlKind;
import org.apache.calcite.sql.SqlNode;

import java.util.HashMap;

/*
Imagination is more important than knowledge. Knowledge is limited. Imagination encircles the world.

-- Albert Einstein, in Saturday Evening Post 26 October 1929
 */

/**
 * SqlService represents a service that handles SQL commands in the Kronotop database system.
 */
public class SqlService extends CommandHandlerService implements KronotopService {
    public static final String NAME = "SQL";
    protected final HashMap<SqlKind, Executor<SqlNode>> ddlExecutors = new HashMap<>();
    private final Context context;
    private final SqlMetadataService metadataService;

    public SqlService(Context context, Handlers handlers) {
        super(context, handlers);
        this.context = context;
        this.metadataService = context.getService(SqlMetadataService.NAME);

        ddlExecutors.put(SqlKind.CREATE_SCHEMA, new CreateSchema(this));
        ddlExecutors.put(SqlKind.CREATE_TABLE, new CreateTable(this));
        ddlExecutors.put(SqlKind.DROP_SCHEMA, new DropSchema(this));
        ddlExecutors.put(SqlKind.DROP_TABLE, new DropTable(this));

        registerHandler(new SqlHandler(this));
        registerHandler(new SqlSetSchemaHandler(this));
        registerHandler(new SqlGetSchemaHandler(this));
    }

    public SqlMetadataService getMetadataService() {
        return metadataService;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Context getContext() {
        return context;
    }

    @Override
    public void shutdown() {

    }
}
