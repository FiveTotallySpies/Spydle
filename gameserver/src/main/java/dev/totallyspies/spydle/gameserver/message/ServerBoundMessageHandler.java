package dev.totallyspies.spydle.gameserver.message;

import dev.totallyspies.spydle.shared.proto.messages.SbMessage;
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
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

@Component
public class ServerBoundMessageHandler implements BeanPostProcessor {

    private final Logger logger = LoggerFactory.getLogger(ServerBoundMessageHandler.class);

    // TODO add priorities?
    private final Map<Class<?>, List<BiConsumer<Object, UUID>>> executors = new HashMap<>();
    private final Map<SbMessage.PayloadCase, Method> payloadGetters = new HashMap<>();

    public ServerBoundMessageHandler() {
        Map<String, Method> methodNames = new HashMap<>();
        for (Method method : SbMessage.class.getDeclaredMethods()) {
            String name = method.getName().toLowerCase();
            if (!name.startsWith("get")) continue;
            name = name.substring(3);
            methodNames.put(name, method);
        }

        for (SbMessage.PayloadCase payloadCase : SbMessage.PayloadCase.values()) {
            if (payloadCase == SbMessage.PayloadCase.PAYLOAD_NOT_SET) continue;
            String name = payloadCase.name().replaceAll("_", "").toLowerCase();
            Method getMethod = methodNames.get(name);
            if (getMethod == null) {
                logger.warn("Warning: Failed to find message class mapping for payloadCase {}, found options {}. " +
                        "Does the proto follow the right naming scheme?",
                        payloadCase.name(), String.join(", ", methodNames.keySet()));
                continue;
            }
            payloadGetters.put(payloadCase, getMethod);
        }
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, @NotNull String beanName) throws BeansException {
        for (Method method : bean.getClass().getMethods()) {
            if (method.isAnnotationPresent(SbMessageListener.class)) {
                SbMessageListener annotation = method.getAnnotation(SbMessageListener.class);
                try {
                    registerListener(annotation.value(), method);
                    logger.debug("Registered ServerBoundMessageListener for payload {} on method {}#{}",
                            annotation.value().name(),
                            method.getDeclaringClass().getCanonicalName(),
                            method.getName());
                } catch (Exception exception) {
                    logger.error("Failed to register ServerBoundMessageListener on method {}#{}",
                            method.getDeclaringClass().getCanonicalName(),
                            method.getName(),
                            exception);
                }
            }
        }
        return bean;
    }

    private void registerListener(SbMessage.PayloadCase payloadCase, Method method) {
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
                if (parameter2.getType() == UUID.class && messageType.equals(parameter1.getType())) {
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

    public void execute(SbMessage message, UUID client) {
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

}