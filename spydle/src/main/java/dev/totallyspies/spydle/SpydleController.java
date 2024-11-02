package dev.totallyspies.spydle;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.protobuf.InvalidProtocolBufferException;

import dev.totallyspies.spydle.proto.MyMessage;
import dev.totallyspies.spydle.proto.MyMessage.ServerBoundMessage;

@RestController
public class SpydleController {

	@PostMapping(path = "/token", consumes = "application/octet-stream", produces = "application/octet-stream")
	public String postToken(@RequestBody byte[] message) throws InvalidProtocolBufferException {
		ServerBoundMessage message2 = MyMessage.ServerBoundMessage.parseFrom(message);
		System.out.println(message2.toString());
		String myToken = "123123";
		return myToken;
	}

	@GetMapping("/newGame")//, produces = "application/x-protobuf")
	public String newGame() {
		return "hello world";
	}

}