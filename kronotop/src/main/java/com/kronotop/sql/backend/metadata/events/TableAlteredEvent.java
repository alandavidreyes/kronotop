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

package com.kronotop.sql.backend.metadata.events;

import java.util.List;

public class TableAlteredEvent extends BaseMetadataEvent {
    private List<String> schema;
    private String table;
    private byte[] versionstamp;

    TableAlteredEvent() {
    }

    public TableAlteredEvent(List<String> schema, String table, byte[] versionstamp) {
        super(EventTypes.TABLE_ALTERED);
        this.schema = schema;
        this.table = table;
        this.versionstamp = versionstamp;
    }

    public List<String> getSchema() {
        return schema;
    }

    public String getTable() {
        return table;
    }

    public byte[] getVersionstamp() {
        return versionstamp;
    }
}
