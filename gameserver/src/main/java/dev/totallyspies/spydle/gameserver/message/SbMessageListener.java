package dev.totallyspies.spydle.gameserver.message;

import dev.totallyspies.spydle.shared.proto.messages.SbMessage;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SbMessageListener {

    SbMessage.PayloadCase value();

}
