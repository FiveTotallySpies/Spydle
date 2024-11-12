package dev.totallyspies.spydle.frontend.client.message;

import dev.totallyspies.spydle.shared.message.MessageHandler;
import dev.totallyspies.spydle.shared.proto.messages.CbMessage;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

@Component
public class CbMessageListenerProcessor implements BeanPostProcessor {

    private final Logger logger = LoggerFactory.getLogger(CbMessageListenerProcessor.class);

    @Getter
    private final MessageHandler<CbMessage, CbMessage.PayloadCase, CbMessageListener> handler;

    private CbMessageListenerProcessor() {
        handler = new MessageHandler<>(CbMessage.class, CbMessageListener.class, logger);
        logger.info("Created client-bound message listener processor");
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        handler.processClass(bean);
        return bean;
    }

}