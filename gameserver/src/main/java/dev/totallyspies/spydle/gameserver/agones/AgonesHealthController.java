package dev.totallyspies.spydle.gameserver.agones;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AgonesHealthController {

    @GetMapping("/health/liveness")
    public String liveness() {
        return "OK";
    }

    @GetMapping("/health/readiness")
    public String readiness() {
        return "OK";
    }

}