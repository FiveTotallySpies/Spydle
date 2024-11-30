package dev.totallyspies.spydle.frontend.client.rest;

import dev.totallyspies.spydle.matchmaker.generated.model.ClientErrorResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

// 4xx: Fires when our Matchmaker says that the request sent to it from us (client) is bad
@AllArgsConstructor
@Getter
public class ClientErrorException extends RuntimeException {

  private ClientErrorResponse response;
  private String message;
  private int code;
}
