package com.hivemq.bootstrap.netty.initializer;

import com.hivemq.bootstrap.netty.ChannelDependencies;
import com.hivemq.configuration.service.entity.QuicListener;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.logging.EventLog;
import com.hivemq.security.ssl.NonSslHandler;
import com.protocol7.quincy.netty.QuicBuilder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;

import javax.inject.Provider;

import static com.hivemq.bootstrap.netty.ChannelHandlerNames.NON_SSL_HANDLER;
import static com.hivemq.bootstrap.netty.ChannelHandlerNames.QUIC_SERVER_HANDLER;

public class QuicChannelInitializer extends AbstractChannelInitializer {

    @NotNull
    private final Provider<NonSslHandler> nonSslHandlerProvider;

    public QuicChannelInitializer(
            @NotNull final ChannelDependencies channelDependencies,
            @NotNull final QuicListener listener,
            @NotNull final Provider<NonSslHandler> nonSslHandlerProvider) {
        super(channelDependencies, listener);
        this.nonSslHandlerProvider = nonSslHandlerProvider;
    }

    @Override
    protected void addSpecialHandlers(@NotNull final Channel ch) {
        ch.pipeline().addFirst(NON_SSL_HANDLER, nonSslHandlerProvider.get());
        final ChannelHandler handler = new QuicBuilder().channelInitializer();
        ch.pipeline().addBefore(NON_SSL_HANDLER, QUIC_SERVER_HANDLER, handler);
    }

}
