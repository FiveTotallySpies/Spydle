package dev.totallyspies.spydle.matchmaker.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientSession implements Serializable {

    private UUID clientId;
    private String gameServerName;

}