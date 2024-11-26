package dev.totallyspies.spydle.frontend.interface_adapters.list_rooms;

import lombok.Data;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Data
@Profile("!local")
public class ListRoomsViewModel {

    private String[] linesInRoomList;

}
