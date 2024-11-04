package dev.totallyspies.spydle;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import dev.totallyspies.spydle.proto.GameMessages.ClientBoundJoinedGame;
import dev.totallyspies.spydle.proto.GameMessages.ClientBoundMessage;
import dev.totallyspies.spydle.proto.GameMessages.ClientBoundNewGame;
import dev.totallyspies.spydle.proto.GameMessages.ClientBoundToken;
import dev.totallyspies.spydle.proto.GameMessages.ServerBoundMessage;

@RestController
public class SpydleController {
	GameLogic gameLogic = new GameLogic();

	@GetMapping(path = "/token", produces = "application/octet-stream")
	public ByteArrayResource getToken() {
		String token = gameLogic.newToken();

		var response = ClientBoundMessage.newBuilder().setClientToken(
			ClientBoundToken.newBuilder().setToken(token)
		);

		return new ByteArrayResource(response.build().toByteArray());
	}

	@PostMapping(path = "/newGame", consumes = "application/octet-stream", produces = "application/octet-stream")
	public ByteArrayResource newGame(@RequestBody byte[] messageBytes) throws Exception {
		var message = ServerBoundMessage.parseFrom(messageBytes);

		String player = gameLogic.validatePlayer(message.getToken());

		String gameId = gameLogic.newGame(player);
		
		var newGame = ClientBoundNewGame.newBuilder().setGameId(gameId);
		var response = ClientBoundMessage.newBuilder().setNewGame(newGame);

		return new ByteArrayResource(response.build().toByteArray());
	}

	@PostMapping(path = "/joinGame", consumes = "application/octet-stream", produces = "application/octet-stream")
	public ByteArrayResource joinGame(@RequestBody byte[] messageBytes) throws Exception {
		var message = ServerBoundMessage.parseFrom(messageBytes);
	
		String player = gameLogic.validatePlayer(message.getToken());

		if (!message.hasJoinGame()) {
			throw new Exception("wrong message, should be join_game");
		}

		String game = gameLogic.validateGame(message.getJoinGame().getGameId()); // getJoinGame doesn't throw an exception

		gameLogic.joinPlayerToGame(player, game);

		var response = ClientBoundMessage.newBuilder().setJoinedGame(
			ClientBoundJoinedGame.newBuilder().setPlayerName(message.getJoinGame().getPlayerName())
		);

		return new ByteArrayResource(response.build().toByteArray());
	}
}