package dev.totallyspies.spydle.matchmaker.config;

import allocation.AllocationServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.util.ClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
public class KubernetesClusterHook {

    private final Logger logger = LoggerFactory.getLogger(KubernetesClusterHook.class);

    @Bean
    public ApiClient k8sClient() throws IOException {
        ApiClient client = ClientBuilder.defaultClient();
        Configuration.setDefaultApiClient(client);
        logger.info("Initialized K8s cluster API client");
        return client;
    }

    @Bean
    public CoreV1Api coreV1Api() {
        return new CoreV1Api();
    }

    @Bean
    public AllocationServiceGrpc.AllocationServiceStub allocationServiceStub(
            @Value("${agones.allocator.port}") int port,
            @Value("${agones.allocator.namespace}") String namespace,
            @Value("${agones.allocator.service-name}") String serviceName,
            @Value("${agones.gameserver.fleet}") String gameFleet,
            @Value("${agones.gameserver.namespace}") String gameNamespace,
            CoreV1Api coreV1Api
    ) throws ApiException {
        // Load clusterIP for Agones allocator service
        V1Service service = coreV1Api.readNamespacedService(serviceName, namespace).execute();
        String allocatorClusterIP = service.getSpec().getClusterIP();
        logger.info("For agones allocator service {} in namespace {}, found target {}:{} for game fleet {} in namespace {}",
                serviceName,
                namespace,
                allocatorClusterIP,
                port,
                gameFleet,
                gameNamespace);

        // Create ManagedChannel
        ManagedChannel managedChannel = ManagedChannelBuilder
                .forAddress(allocatorClusterIP, port)
                .usePlaintext()
                .enableRetry()
                .keepAliveTime(10, TimeUnit.SECONDS)
                .build();

        // Return AllocationServiceStub
        AllocationServiceGrpc.AllocationServiceStub stub = AllocationServiceGrpc.newStub(managedChannel);
        logger.info("Created allocation service GRPC stub");
        return stub;
    }

}
