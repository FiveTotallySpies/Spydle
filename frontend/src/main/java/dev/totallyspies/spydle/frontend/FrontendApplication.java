package dev.totallyspies.spydle.frontend;

import dev.totallyspies.spydle.frontend.interface_adapters.view_manager.ViewManagerModel;
import java.awt.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class FrontendApplication {

  public static void main(String[] args) {
    var ctx = new SpringApplicationBuilder(FrontendApplication.class).headless(false).run(args);

    EventQueue.invokeLater(
        () -> {
          var gameView = ctx.getBean(ViewManagerModel.class);
          gameView.setVisible(true);
        });
  }
}
