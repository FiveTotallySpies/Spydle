package dev.totallyspies.spydle.frontend.client;

import dev.totallyspies.spydle.frontend.client.message.CbMessageListenerProcessor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class ClientSocketConfig {

    private final ApplicationEventPublisher publisher;
    private final CbMessageListenerProcessor processor;

    public ClientSocketConfig(ApplicationEventPublisher publisher, CbMessageListenerProcessor processor) {
        this.publisher = publisher;
        this.processor = processor;
    }

    @Bean
    @Profile("!local")
    public ClientSocketHandler clientSocketHandler() {
        return createClient();
    }

    public ClientSocketHandler createClient() {
        return new ClientSocketHandler(processor, publisher);
    }

}
