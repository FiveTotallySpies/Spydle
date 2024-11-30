package dev.totallyspies.spydle.frontend.test;

import dev.totallyspies.spydle.frontend.client.ClientSocketHandler;
import dev.totallyspies.spydle.shared.proto.messages.SbMessage;
import java.util.UUID;
import lombok.Data;

@Data
public class TestPlayer {
  private String name;
  private ClientSocketHandler handler;
  private UUID uuid;

  public TestPlayer(String name, UUID uuid, ClientSocketHandler handler) {
    this.name = name;
    this.uuid = uuid;
    this.handler = handler;
  }

  public void open(String address, int port) {
    this.handler.open(address, port, uuid, name);
  }

  public void send(SbMessage message) {
    this.handler.sendSbMessage(message);
  }
}
