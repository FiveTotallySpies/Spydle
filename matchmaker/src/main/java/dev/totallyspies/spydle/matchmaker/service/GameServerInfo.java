package dev.totallyspies.spydle.matchmaker.service;

import dev.totallyspies.spydle.matchmaker.generated.model.GameServerInfoModel;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

/**
 * Info we have on each game server that agones spins up
 */
@Data
@Builder
@ToString
public class GameServerInfo {

    private String address;
    private int port;
    private String gameServerName;

    public GameServerInfoModel toModel() {
        return new GameServerInfoModel().gameServerName(gameServerName).address(address).port(port);
    }

}
