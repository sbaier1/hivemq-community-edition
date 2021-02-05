package com.hivemq.bootstrap.netty.initializer;

import com.hivemq.bootstrap.netty.ChannelDependencies;
import com.hivemq.configuration.service.entity.QuicListener;
import com.hivemq.extension.sdk.api.annotations.NotNull;
import com.hivemq.security.ssl.NonSslHandler;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Provider;

import static com.hivemq.bootstrap.netty.ChannelHandlerNames.NON_SSL_HANDLER;

public class QuicChannelInitializer extends AbstractChannelInitializer {
    private static final @NotNull Logger log = LoggerFactory.getLogger(QuicChannelInitializer.class);


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
    }

}
