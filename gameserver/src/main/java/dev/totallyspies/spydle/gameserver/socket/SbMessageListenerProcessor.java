package dev.totallyspies.spydle.gameserver.socket;

import dev.totallyspies.spydle.shared.message.MessageHandler;
import dev.totallyspies.spydle.shared.proto.messages.SbMessage;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

@Component
public class SbMessageListenerProcessor implements BeanPostProcessor {

  private final Logger logger = LoggerFactory.getLogger(SbMessageListenerProcessor.class);

  @Getter private final MessageHandler<SbMessage, SbMessage.PayloadCase, SbMessageListener> handler;

  private SbMessageListenerProcessor() {
    handler = new MessageHandler<>(SbMessage.class, SbMessageListener.class, logger);
    logger.info("Created server-bound message listener processor");
  }

  @Override
  public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    handler.processBean(bean);
    return bean;
  }
}
