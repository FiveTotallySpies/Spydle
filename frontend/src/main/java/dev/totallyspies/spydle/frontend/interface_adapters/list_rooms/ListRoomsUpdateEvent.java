package dev.totallyspies.spydle.frontend.interface_adapters.list_rooms;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

@Getter
public class ListRoomsUpdateEvent extends ApplicationEvent {

    private final List<String> roomCodes;

    public ListRoomsUpdateEvent(Object source, List<String> roomCodes) {
        super(source);
        this.roomCodes = roomCodes;
    }

}