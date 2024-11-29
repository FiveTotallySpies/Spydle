package dev.totallyspies.spydle.frontend.interface_adapters.list_rooms;

import java.util.List;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ListRoomsUpdateEvent extends ApplicationEvent {

  private final List<String> roomCodes;

  public ListRoomsUpdateEvent(Object source, List<String> roomCodes) {
    super(source);
    this.roomCodes = roomCodes;
  }
}
