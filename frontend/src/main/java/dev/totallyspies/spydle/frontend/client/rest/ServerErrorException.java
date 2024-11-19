package dev.totallyspies.spydle.frontend.client.rest;

import lombok.AllArgsConstructor;
import lombok.Getter;

// 5xx: Fired when our Matchmaker has an internal error
@AllArgsConstructor
@Getter
public class ServerErrorException extends RuntimeException {

    private String message;
    private int code;

}