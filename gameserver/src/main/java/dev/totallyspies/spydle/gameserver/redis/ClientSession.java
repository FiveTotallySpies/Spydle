package dev.totallyspies.spydle.gameserver.redis;

import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ClientSession implements Serializable {

    private UUID clientId;
    private String gameServerName;

}