package dev.totallyspies.spydle.gameserver.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/health/liveness")
    public String liveness() {
        return "OK";
    }

    @GetMapping("/health/readiness")
    public String readiness() {
        return "OK";
    }

}