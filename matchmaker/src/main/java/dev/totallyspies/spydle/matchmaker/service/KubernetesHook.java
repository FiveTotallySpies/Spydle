package dev.totallyspies.spydle.matchmaker.service;

import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.util.ClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class KubernetesHook {

    @Bean
    public ApiClient k8sClient() throws IOException {
        ApiClient client = ClientBuilder.defaultClient();
        Configuration.setDefaultApiClient(client);
        return client;
    }

}
