package dev.totallyspies.spydle.frontend.client.rest;

import java.net.InetSocketAddress;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfiguration {

  private final String baseUrl;
  private final InetSocketAddress hostAddress;

  public WebClientConfiguration(
      @Value("${server.backend.host}") String hostHeaderIp,
      @Value("${server.backend.url}") String backendUrl,
      @Value("${server.backend.port}") int backendPort) {
    baseUrl = backendUrl + ":" + backendPort;
    hostAddress = InetSocketAddress.createUnresolved(hostHeaderIp, backendPort);
  }

  @Bean
  public WebClient webClient(WebClient.Builder builder) {
    return builder
        .baseUrl(baseUrl)
        .defaultHeaders(headers -> headers.setHost(hostAddress))
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .build();
  }
}
