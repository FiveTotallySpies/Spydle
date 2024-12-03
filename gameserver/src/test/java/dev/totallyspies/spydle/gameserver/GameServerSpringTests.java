package dev.totallyspies.spydle.gameserver;

import dev.totallyspies.spydle.gameserver.agones.AgonesConfig;
import dev.totallyspies.spydle.gameserver.agones.AgonesHook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest(properties = "spring.config.location=classpath:application-local.properties")
@ExtendWith(SpringExtension.class)
public class GameServerSpringTests {

  @MockBean private AgonesHook agonesHook;

  @MockBean private AgonesConfig agonesConfig;

  @Test
  public void contextLoads() {
    // This test will pass if the application context loads successfully
  }
}
