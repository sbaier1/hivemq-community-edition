/*
 * Copyright 2019 dc-square GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hivemq.configuration.service.entity;

import com.hivemq.extension.sdk.api.annotations.Immutable;
import com.hivemq.extension.sdk.api.annotations.NotNull;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A listener which allows to listen to MQTT traffic via QUIC
 *
 * @author Dominik Obermaier
 * @author Christoph Schaebel
 * @author Georg Held
 * @author Simon Baier
 */
@Immutable
public class QuicListener implements Listener {

    private final int port;

    private final @NotNull String bindAddress;
    private final @NotNull String name;

    /**
     * Creates a new QUIC listener which listens to a specific port and bind address
     *
     * @param port        the port
     * @param bindAddress the bind address
     */
    public QuicListener(final int port, @NotNull final String bindAddress) {
        this(port, bindAddress, "tcp-listener-" + port);
    }

    /**
     * Creates a new QUIC listener which listens to a specific port and bind address
     *
     * @param port        the port
     * @param bindAddress the bind address
     * @param name        the name of the listener
     */
    public QuicListener(final int port, @NotNull final String bindAddress, @NotNull final String name) {

        checkNotNull(bindAddress, "bindAddress must not be null");

        this.port = port;
        this.bindAddress = bindAddress;
        this.name = name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getPort() {
        return port;
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public String getBindAddress() {
        return bindAddress;
    }

    /**
     * {@inheritDoc}
     */
    @NotNull
    @Override
    public String readableName() {
        return "QUIC Listener";
    }

    @Override
    public @NotNull String getName() {
        return name;
    }

}
