package dev.totallyspies.spydle.frontend.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class ClientSocketConfig {

    @Autowired
    private ApplicationContext context;

    @Bean
    @Profile("!local")
    public ClientSocketHandler clientSocketHandler() {
        return new ClientSocketHandler(context);
    }

}
