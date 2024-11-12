package dev.totallyspies.spydle.shared.message;

import org.slf4j.Logger;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

/**
 * This class can act as mostly a black box for processing the protobuf-generated schema.
 * Given a MessageType (should be either CbMessage or SbMessage and an annotation (you create, like @SbMessageListener),
 * this class can:
 * 1) Scan a given class for all instances of methods with this annotation
 * 2) Map the arguments of these methods to other proto-generated corresponding classes
 * 3) Run all of these methods on execute() given a message, and a clientId
 * We use a lot of reflection and generics to achieve this.
 */
@SuppressWarnings("unchecked")
public class MessageHandler<MessageType, PayloadCaseType extends Enum<?>, AnnotationType extends Annotation> {

    private final Class<MessageType> messageClass;
    private final Method messageClassGetPayloadCase;
    private final Class<AnnotationType> annotationClass;
    private final Logger logger;

    // TODO add priorities?
    // Maps between: Class of type Sb<MESSAGE_TYPE> (like SbStartGame) and a list of executors for it
    // Executor consumes: Message itself (SbStartGame), and client ID
    private final Map<Class<?>, List<BiConsumer<Object, UUID>>> executors = new HashMap<>();
    // Map between PayloadCase (like PayloadCase.START_GAME) and getter method from SbMessage (like SbMessage#getStartGame)
    private final Map<PayloadCaseType, Method> payloadGetters = new HashMap<>();
    // Map between: Class of type Sb<MESSAGE_TYPE> and PayloadCase
    private final Map<Class<?>, PayloadCaseType> messageToPayload = new HashMap<>();

    public MessageHandler(Class<MessageType> messageClass,
                          Class<AnnotationType> annotationClass,
                          Logger logger) {
        this(messageClass,
                (Class<PayloadCaseType>) Arrays.stream(messageClass.getDeclaredClasses())
                        .filter(target -> target.getSimpleName().equals("PayloadCase") && target.isEnum())
                        .findFirst()
                        .orElseThrow(),
                annotationClass,
                logger);
    }


    public MessageHandler(Class<MessageType> messageClass,
                          Class<PayloadCaseType> payloadCaseClass,
                          Class<AnnotationType> annotationClass,
                          Logger logger) {
        this.messageClass = messageClass;
        this.annotationClass = annotationClass;
        this.logger = logger;
        try {
            // Check methods exist first
            this.messageClassGetPayloadCase = messageClass.getMethod("getPayloadCase");

            // Using reflection, gets all method names for each of the payload case types
            Map<String, Method> methodNames = new HashMap<>();
            for (Method method : messageClass.getDeclaredMethods()) {
                // Name like "joingame"
                String name = method.getName().toLowerCase();
                if (!name.startsWith("get")) continue;
                name = name.substring(3);
                methodNames.put(name, method);
            }

            // Maps between a payload case and method for getting that message type from SbMessage
            for (PayloadCaseType payloadCase : payloadCaseClass.getEnumConstants()) {
                if (payloadCase.name().contains("PAYLOAD_NOT_SET")) continue;
                String name = payloadCase.name().replaceAll("_", "").toLowerCase();
                Method getMethod = methodNames.get(name);
                if (getMethod == null) {
                    logger.warn("Warning: Failed to find message class mapping for payloadCase {}, found options {}. " +
                                    "Does the proto follow the right naming scheme?",
                            payloadCase.name(), String.join(", ", methodNames.keySet()));
                    continue;
                }
                messageToPayload.put(getMethod.getReturnType(), payloadCase);
                payloadGetters.put(payloadCase, getMethod);
            }
        } catch (Exception exception) {
            throw new RuntimeException("Failed to instantiate message handler", exception);
        }
    }

    public void processBean(Object bean) {
        for (Method method : bean.getClass().getMethods()) {
            if (method.isAnnotationPresent(annotationClass)) {
                try {
                    registerListener(bean, method);
                    logger.debug("Registered {} for message {} on method {}#{}",
                            messageClass.getName(),
                            method.getParameters()[0].getType().getName(),
                            method.getDeclaringClass().getCanonicalName(),
                            method.getName());
                } catch (Exception exception) {
                    logger.error("Failed to register {} on method {}#{}",
                            annotationClass.getName(),
                            method.getDeclaringClass().getCanonicalName(),
                            method.getName(),
                            exception);
                }
            }
        }
    }

    private void registerListener(Object bean, Method method) {
        if (method.getParameterCount() == 2
                && method.getParameters()[1].getType() == UUID.class) {
            Class<?> messageType = method.getParameters()[0].getType();
            PayloadCaseType payloadCase = messageToPayload.get(messageType);
            if (payloadCase == null) throw new IllegalArgumentException("Unknown message type " + messageType.getCanonicalName());

            Method messageGetter = payloadGetters.get(payloadCase);
            if (messageGetter == null) throw new IllegalArgumentException("Unknown message payload " + payloadCase.name());

            registerExecutor(messageType, (message, client) -> {
                try {
                    method.invoke(message, client);
                } catch (Exception exception) {
                    throw new RuntimeException(exception);
                }
            });
            return;
        }
        throw new IllegalArgumentException("Message Listener must contain just " + messageClass.getName() + " payload client UUID)!");
    }

    private void registerExecutor(Class<?> messageType, BiConsumer<Object, UUID> executor) {
        if (!executors.containsKey(messageType)) {
            executors.put(messageType, new ArrayList<>());
        }
        executors.get(messageType).add(executor);
    }

    private Collection<BiConsumer<Object, UUID>> getExecutors(Class<?> messageType) {
        if (executors.containsKey(messageType)) {
            return executors.get(messageType);
        }
        return Collections.EMPTY_SET;
    }

    public void execute(MessageType message, UUID client) {
        try {
            PayloadCaseType payloadCase = (PayloadCaseType) messageClassGetPayloadCase.invoke(message);
            Method methodGetter = payloadGetters.get(payloadCase);
            if (methodGetter == null) {
                logger.warn("Failing to execute unknown SbMessage {} with PayloadCase {}", message.getClass().getName(), payloadCase.name());
                return;
            }
            Object subMessage = methodGetter.invoke(message);
            getExecutors(subMessage.getClass()).forEach(executor -> executor.accept(subMessage, client));

            logger.debug("Fired message with PayloadCase {} from client {}", payloadCase, client);
        } catch (IllegalAccessException | InvocationTargetException exception) {
            throw new RuntimeException("Failed to execute message", exception);
        }
    }

}