package dev.totallyspies.spydle.matchmaker;

import allocation.AllocationServiceGrpc;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest(properties = "spring.config.location=classpath:application-test.properties")
@ExtendWith(SpringExtension.class)
public class MatchmakerSpringTest {

  @MockBean private ApiClient k8sClient;

  @MockBean private CoreV1Api coreV1Api;

  @MockBean private AllocationServiceGrpc.AllocationServiceStub allocationServiceStub;

  @Test
  public void contextLoads() {
    // This test will pass if the application context loads successfully
  }
}
