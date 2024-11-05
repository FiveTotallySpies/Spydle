package dev.totallyspies.spydle.gameserver.message;

import dev.totallyspies.spydle.shared.proto.messages.SbMessage;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
public class SbMessageHandler implements BeanPostProcessor {

    private final Logger logger = LoggerFactory.getLogger(SbMessageHandler.class);

    // TODO add priorities?
    // Maps between: Class of type Sb<MESSAGE_TYPE> (like SbStartGame) and a list of executors for it
    // Executor consumes: Message itself (SbStartGame), and client ID
    private final Map<Class<?>, List<BiConsumer<Object, UUID>>> executors = new HashMap<>();
    // Map between PayloadCase (like PayloadCase.START_GAME) and getter method from SbMessage (like SbMessage#getStartGame)
    private final Map<SbMessage.PayloadCase, Method> payloadGetters = new HashMap<>();

    public SbMessageHandler() {
        // Using reflection, gets all method names for each of the payload case types
        Map<String, Method> methodNames = new HashMap<>();
        for (Method method : SbMessage.class.getDeclaredMethods()) {
            // Name like "joingame"
            String name = method.getName().toLowerCase();
            if (!name.startsWith("get")) continue;
            name = name.substring(3);
            methodNames.put(name, method);
        }

        // Maps between a payload case and method for getting that message type from SbMessage
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

    // Loop through all beans that have been initialized
    @Override
    public Object postProcessAfterInitialization(Object bean, @NotNull String beanName) throws BeansException {
        for (Method method : bean.getClass().getMethods()) {
            if (method.isAnnotationPresent(SbMessageListener.class)) {
                // Find SbMessageListener annotation
                SbMessageListener annotation = method.getAnnotation(SbMessageListener.class);
                try {
                    registerListener(annotation.value(), method);
                    logger.debug("Registered SbMessageListener for payload {} on method {}#{}",
                            annotation.value().name(),
                            method.getDeclaringClass().getCanonicalName(),
                            method.getName());
                } catch (Exception exception) {
                    logger.error("Failed to register SbMessageListener on method {}#{}",
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
        if (method.getParameterCount() == 2
                && messageType.equals(method.getParameters()[0].getType())
                && method.getParameters()[1].getType() == UUID.class) {
            registerExecutor(messageType, (message, client) -> {
                try {
                    method.invoke(message, client);
                } catch (IllegalAccessException | InvocationTargetException exception) {
                    throw new RuntimeException(exception);
                }
            });
            return;
        }
        throw new IllegalArgumentException("Message Listener must contain just SbMessage payload client UUID)!");
    }

    private void registerExecutor(Class<?> messageType, BiConsumer<Object, UUID> executor) {
        if (!executors.containsKey(messageType)) {
            executors.put(messageType, new ArrayList<>());
        }
        executors.get(messageType).add(executor);
    }

    private Collection<BiConsumer<Object, UUID>> getExecutors(Class<?> messageType) {
        if (!executors.containsKey(messageType)) {
            throw new IllegalArgumentException("Unknown message type " + messageType.getCanonicalName());
        }
        return executors.get(messageType);
    }

    public void execute(SbMessage message, UUID client) {
        Method methodGetter = payloadGetters.get(message.getPayloadCase());
        if (methodGetter == null) {
            logger.warn("Failing to execute unknown SbMessage {} with PayloadCase {}", message.getClass().getName(), message.getPayloadCase().name());
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