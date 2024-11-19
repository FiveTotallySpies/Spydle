package dev.totallyspies.spydle.frontend.client;

import dev.totallyspies.spydle.frontend.client.message.CbMessageListenerProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class ClientSocketConfig {

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private CbMessageListenerProcessor processor;

    @Bean
    @Profile("!local")
    public ClientSocketHandler clientSocketHandler() {
        return createClient();
    }

    public ClientSocketHandler createClient() {
        return new ClientSocketHandler(processor, publisher);
    }

}
