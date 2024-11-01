package dev.totallyspies.spydle.gameserver.socket.event;

import dev.totallyspies.spydle.shared.proto.GameMessages;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class ServerBoundMessageHandler implements BeanPostProcessor {

    private final Logger logger = LoggerFactory.getLogger(ServerBoundMessageHandler.class);

    @Autowired
    private Map<GameMessages.ServerBoundMessage.PayloadCase, Method> payloadGetters;

    // TODO add priorities?
    private final Map<Class<?>, List<BiConsumer<Object, UUID>>> executors = new HashMap<>();

    @Override
    public Object postProcessAfterInitialization(Object bean, @NotNull String beanName) throws BeansException {
        for (Method method : bean.getClass().getMethods()) {
            if (method.isAnnotationPresent(ServerBoundMessageListener.class)) {
                ServerBoundMessageListener annotation = method.getAnnotation(ServerBoundMessageListener.class);
                try {
                    registerListener(annotation.value(), method);
                    logger.debug("Registered ServerBoundMessageListener for payload {} on method {}#{}", annotation.value().name(), method.getClass().getCanonicalName(), method.getName());
                } catch (Exception exception) {
                    logger.error("Failed to register ServerBoundMessageListener on method {}#{}", method.getClass().getCanonicalName(), method.getName(), exception);
                }
            }
        }
        return bean;
    }

    private void registerListener(GameMessages.ServerBoundMessage.PayloadCase payloadCase, Method method) {
        Method messageGetter = payloadGetters.get(payloadCase);
        if (messageGetter == null) throw new IllegalArgumentException("Unknown message type " + payloadCase.name());
        Class<?> messageType = messageGetter.getReturnType();
        switch (method.getParameterCount()) {
            case 1:
                Parameter parameter = method.getParameters()[0];
                if (messageType.isInstance(parameter.getType())) {
                    registerExecutor(messageType, (message, client) -> {
                        try {
                            method.invoke(message);
                        } catch (IllegalAccessException | InvocationTargetException exception) {
                            throw new RuntimeException(exception);
                        }
                    });
                    return;
                }
            case 2:
                Parameter parameter1 = method.getParameters()[0];
                Parameter parameter2 = method.getParameters()[1];
                if (parameter2.getType() == UUID.class && messageType.isInstance(parameter1.getType())) {
                    registerExecutor(messageType, (message, client) -> {
                        try {
                            method.invoke(message, client);
                        } catch (IllegalAccessException | InvocationTargetException exception) {
                            throw new RuntimeException(exception);
                        }
                    });
                    return;
                } else if (parameter1.getType() == UUID.class && messageType.isInstance(parameter2.getType())) {
                    registerExecutor(messageType, (message, client) -> {
                        try {
                            method.invoke(client, message);
                        } catch (IllegalAccessException | InvocationTargetException exception) {
                            throw new RuntimeException(exception);
                        }
                    });
                    return;
                }
            default:
                throw new IllegalArgumentException("Message Listener must contain ServerBoundMessage payload (and possible client UUID)!");
        }
    }

    private void registerExecutor(Class<?> messageType, BiConsumer<Object, UUID> executor) {
        if (!executors.containsKey(messageType)) {
            executors.put(messageType, new ArrayList<>());
        }
        executors.get(messageType).add(executor);
    }

    private Collection<BiConsumer<Object, UUID>> getExecutors(Class<?> messageType) {
        return executors.get(messageType);
    }

    public void execute(GameMessages.ServerBoundMessage message, UUID client) {
        Method methodGetter = payloadGetters.get(message.getPayloadCase());
        if (methodGetter == null) {
            logger.warn("Failing to execute unknown ServerBoundMessage {} with PayloadCase {}", message.getClass().getName(), message.getPayloadCase().name());
            return;
        }
        try {
            Object subMessage = methodGetter.invoke(message);
            getExecutors(subMessage.getClass()).forEach(executor -> executor.accept(subMessage, client));
        } catch (IllegalAccessException | InvocationTargetException exception) {
            throw new RuntimeException(exception);
        }
        logger.debug("Fired message with PayloadCase {} from client {}", message.getPayloadCase(), client);
    }

    @Bean
    public Map<GameMessages.ServerBoundMessage.PayloadCase, Method> payloadGetters() {
        Map<String, Method> methodNames = new HashMap<>();
        for (Method method : GameMessages.ServerBoundMessage.class.getDeclaredMethods()) {
            String name = method.getName().toLowerCase();
            if (!name.startsWith("get")) continue;
            name = name.substring(3);
            methodNames.put(name, method);
        }

        Map<GameMessages.ServerBoundMessage.PayloadCase, Method> payloadGetters = new HashMap<>();
        for (GameMessages.ServerBoundMessage.PayloadCase payloadCase : GameMessages.ServerBoundMessage.PayloadCase.values()) {
            String name = payloadCase.name().replaceAll("_", "");
            Method getMethod = methodNames.get(name);
            if (getMethod == null) {
                logger.warn("Warning: Failed to find message class mapping for payloadCase {}. Does the proto follow the right naming scheme?", payloadCase.name());
                continue;
            }
            payloadGetters.put(payloadCase, getMethod);
        }

        return payloadGetters;
    }

}