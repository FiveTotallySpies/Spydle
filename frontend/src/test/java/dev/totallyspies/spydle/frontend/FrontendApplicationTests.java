package dev.totallyspies.spydle.frontend;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest(properties = "spring.config.location=classpath:application-test.properties")
@ExtendWith(SpringExtension.class)
public class FrontendApplicationTests {

  @Test
  void contextLoads() {
    // This test will pass if the application context loads successfully
  }
}
