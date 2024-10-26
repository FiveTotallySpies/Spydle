package dev.totallyspies.spydle.matchmaker.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientSession {

    private String clientId;
    private String gameServerName;

}