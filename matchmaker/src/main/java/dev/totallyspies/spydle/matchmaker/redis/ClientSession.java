package dev.totallyspies.spydle.matchmaker.redis;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

@Data
@AllArgsConstructor
public class ClientSession implements Serializable {

    private UUID clientId;
    private String gameServerName;

}