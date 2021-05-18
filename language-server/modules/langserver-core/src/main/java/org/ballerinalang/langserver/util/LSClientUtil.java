/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.util;

import org.ballerinalang.langserver.commons.LanguageServerContext;
import org.ballerinalang.langserver.commons.capability.LSClientCapabilities;
import org.ballerinalang.langserver.commons.client.ExtendedLanguageClient;
import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.ExecuteCommandOptions;
import org.eclipse.lsp4j.Registration;
import org.eclipse.lsp4j.RegistrationParams;
import org.eclipse.lsp4j.ServerCapabilities;
import org.eclipse.lsp4j.Unregistration;
import org.eclipse.lsp4j.UnregistrationParams;
import org.eclipse.lsp4j.services.LanguageClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * A collection of utilities related to LS client.
 *
 * @since 2.0.0
 */
public class LSClientUtil {

    public static final String EXECUTE_COMMAND_CAPABILITY_ID = UUID.randomUUID().toString();

    private LSClientUtil() {

    }

    public static boolean chekAndRegisterCommands(List<String> commands, LanguageServerContext context) {
        ServerCapabilities serverCapabilities = context.get(ServerCapabilities.class);
        if (serverCapabilities.getExecuteCommandProvider() == null) {
            return false;
        }

        if (!isDynamicCommandRegistrationSupported(context)) {
            return false;
        }

        Set<String> supportedCommands = new HashSet<>(serverCapabilities.getExecuteCommandProvider().getCommands());
        boolean shouldReRegister = commands.stream().anyMatch(command -> !supportedCommands.contains(command));

        if (shouldReRegister) {
            supportedCommands.addAll(commands);
            unregisterCommands(context);
            registerCommands(context, new ArrayList<>(supportedCommands));
        }

        return true;
    }

    /**
     * Check if the LS client support dynamic command registration.
     *
     * @param serverContext Language server context
     * @return True if dynamic command registration is supported
     */
    public static boolean isDynamicCommandRegistrationSupported(LanguageServerContext serverContext) {
        LSClientCapabilities clientCapabilities = serverContext.get(LSClientCapabilities.class);
        return Boolean.TRUE.equals(clientCapabilities.getWorkspaceCapabilities().getExecuteCommand()
                .getDynamicRegistration());
    }

    /**
     * Check if the LS client support dynamic command registration.
     *
     * @param clientCapabilities LS client capabilities
     * @return True if dynamic command registration is supported
     */
    public static boolean isDynamicCommandRegistrationSupported(ClientCapabilities clientCapabilities) {
        return Boolean.TRUE.equals(clientCapabilities.getWorkspace().getExecuteCommand().getDynamicRegistration());
    }

    /**
     * Registers the provided set of commands at the LS Client.
     *
     * @param serverContext Language server context
     */
    public static void registerCommands(LanguageServerContext serverContext, List<String> commands) {
        LanguageClient client = serverContext.get(ExtendedLanguageClient.class);
        ExecuteCommandOptions executeCommandOptions = new ExecuteCommandOptions(commands);
        client.registerCapability(new RegistrationParams(Collections.singletonList(
                new Registration(EXECUTE_COMMAND_CAPABILITY_ID, "workspace/executeCommand", executeCommandOptions))));

        ServerCapabilities serverCapabilities = serverContext.get(ServerCapabilities.class);
        // Command provider can be null when invoked for the first time
        if (serverCapabilities.getExecuteCommandProvider() == null) {
            ExecuteCommandOptions provider = new ExecuteCommandOptions(Collections.synchronizedList(commands));
            serverCapabilities.setExecuteCommandProvider(provider);
        } else {
            serverCapabilities.getExecuteCommandProvider().getCommands().addAll(commands);
        }
    }

    /**
     * Registers the provided set of commands at the LS Client.
     *
     * @param serverContext Language server context
     */
    public static void unregisterCommands(LanguageServerContext serverContext) {
        ExtendedLanguageClient extendedLanguageClient = serverContext.get(ExtendedLanguageClient.class);
        extendedLanguageClient.unregisterCapability(new UnregistrationParams(Collections.singletonList(
                new Unregistration(EXECUTE_COMMAND_CAPABILITY_ID, "workspace/executeCommand"))));
        serverContext.get(ServerCapabilities.class).getExecuteCommandProvider().getCommands().clear();
    }
}
