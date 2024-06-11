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

package com.kronotop.server;

import com.kronotop.common.KronotopException;
import com.kronotop.common.resp.RESPError;

/**
 * NoProtoException is a custom exception class that is thrown when an unsupported protocol version is encountered.
 * It is a subclass of KronotopException.
 */
public class NoProtoException extends KronotopException {
    public NoProtoException() {
        super(RESPError.NOPROTO, RESPError.UNSUPPORTED_PROTOCOL_VERSION);
    }
}
