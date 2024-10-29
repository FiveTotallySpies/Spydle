package dev.totallyspies.spydle.matchmaker.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClientSession implements Serializable {

    private String clientId;
    private String gameServerName;

}