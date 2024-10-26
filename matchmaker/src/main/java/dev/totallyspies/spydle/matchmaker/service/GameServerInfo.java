package dev.totallyspies.spydle.matchmaker.service;

import lombok.Data;

/**
 * Info we have on each game server that agones spins up
 */
@Data
public class GameServerInfo {

    private String address;
    private int port;
    private String gameServerName;

}
