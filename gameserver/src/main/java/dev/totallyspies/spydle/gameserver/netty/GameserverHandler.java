package dev.totallyspies.spydle.gameserver.netty;

import dev.totallyspies.spydle.gameserver.service.SessionService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.springframework.beans.factory.annotation.Autowired;

public class GameserverHandler extends SimpleChannelInboundHandler<String> {

    @Autowired
    private SessionService sessionService;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // Handle new client connection
        String clientId = getClientId(ctx);
        // TODO set in K8s config?
        String gameServerName = System.getenv("GAMESERVER_NAME");

        // Check if client has a session in Redis
        if (!sessionService.isClientAssignedToGameServer(clientId, gameServerName)) {
            ctx.close();
            return;
        }

        // If this is the first client, signal ALLOCATED to Agones
        // TODO: Implement logic to check if this is the first client and signal allocation
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        // TODO Handle incoming messages from client
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // TODO Handle client disconnect
        String clientId = getClientId(ctx);
        sessionService.removeClientSession(clientId);
    }

    private String getClientId(ChannelHandlerContext ctx) {
        // Extract client ID from context or authentication token
        // For simplicity, we'll mock this value
        // TODO fix
        return "client-id";
    }
}