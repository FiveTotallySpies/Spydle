package dev.totallyspies.spydle.frontend;

import dev.totallyspies.spydle.frontend.client.ClientSocketHandler;
import dev.totallyspies.spydle.shared.proto.messages.SbMessage;
import dev.totallyspies.spydle.shared.proto.messages.SbSelectName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import java.util.Scanner;
import java.util.UUID;

@Component
public class TestClient {

    @Autowired
    private ClientSocketHandler handler;

    // Executed on application startup, but could be instead on the press of a button?
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        Scanner in = new Scanner(System.in);
        System.out.println("Enter IP:");
        String ip = in.next();
        System.out.println("Enter PORT:");
        int port = in.nextInt();
        System.out.println("Enter CLIENT ID:");
        UUID clientId = UUID.fromString(in.next());
        handler.open(ip, port, clientId); // opne the websocket
        System.out.println("Handler open: " + handler.isOpen());
        // Sample: Send join message
        handler.sendSbMessage(SbMessage.newBuilder().setSelectName(SbSelectName.newBuilder().setPlayerName("kai")).build());
        // Close the websocket
        handler.close();
    }

    // Custom event fired after socket close
    @EventListener(ClientSocketHandler.CloseEvent.class)
    public void onSocketClose(ClientSocketHandler.CloseEvent event) {
        UUID clientId = event.getClientId();
        CloseStatus reason = event.getStatus();
        // Close window, do other logic?
    }
}