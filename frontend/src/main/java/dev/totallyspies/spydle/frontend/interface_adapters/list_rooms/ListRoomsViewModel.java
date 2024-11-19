package dev.totallyspies.spydle.frontend.interface_adapters.list_rooms;

import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class ListRoomsViewModel {

    private String[] linesInRoomList;

}
