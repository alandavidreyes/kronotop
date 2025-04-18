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

package com.kronotop;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kronotop.commands.CommandMetadata;
import com.kronotop.server.CommandAlreadyRegisteredException;
import com.kronotop.server.CommandHandlerRegistry;
import com.kronotop.server.Handler;
import com.kronotop.server.ServerKind;
import com.kronotop.server.annotation.Command;
import com.kronotop.server.annotation.Commands;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

/**
 * The CommandHandlerService class is a base class that handles the registration of command handlers and the loading
 * of command definitions from JSON files.
 */
public class CommandHandlerService extends BaseKronotopService {

    public CommandHandlerService(Context context, final String name) {
        super(context, name);
    }

    /**
     * Registers the given command handlers.
     *
     * @param handlers the handlers to register
     * @throws CommandAlreadyRegisteredException if a command is already registered
     */
    protected void handlerMethod(ServerKind kind, Handler... handlers) throws CommandAlreadyRegisteredException {
        CommandHandlerRegistry registry = context.getHandlers(kind);
        for (Handler handler : handlers) {
            Commands commands = handler.getClass().getAnnotation(Commands.class);
            if (commands != null) {
                for (Command command : commands.value()) {
                    registry.handlerMethod(command.value().toUpperCase(), handler);
                    loadDefinition(command.value().toUpperCase());
                }
            } else {
                Command command = handler.getClass().getAnnotation(Command.class);
                registry.handlerMethod(command.value().toUpperCase(), handler);
                loadDefinition(command.value().toUpperCase());
            }
        }
    }

    /**
     * Loads the definition of a command from a JSON file and registers it in the context.
     *
     * @param command the name of the command
     */
    private void loadDefinition(String command) {
        ClassLoader classLoader = getClass().getClassLoader();
        try (InputStream inputStream = classLoader.getResourceAsStream(String.format("commands/%s.json", command.toLowerCase()))) {
            if (inputStream == null) {
                return;
            }
            byte[] jsonData = inputStream.readAllBytes();
            ObjectMapper objectMapper = new ObjectMapper();
            HashMap<String, CommandMetadata> data = objectMapper.readValue(jsonData, new TypeReference<>() {
            });
            for (String cmd : data.keySet()) {
                context.registerCommandMetadata(cmd.toUpperCase(), data.get(cmd));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
