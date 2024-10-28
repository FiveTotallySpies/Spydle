package dev.totallyspies.spydle.matchmaker.service;

import lombok.Data;
import lombok.ToString;

/**
 * Info we have on each game server that agones spins up
 */
@Data
@ToString
public class GameServerInfo {

    private String address;
    private int port;
    private String gameServerName;

}
