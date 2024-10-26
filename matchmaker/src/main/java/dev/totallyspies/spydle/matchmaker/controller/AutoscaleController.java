package dev.totallyspies.spydle.matchmaker.controller;

import dev.totallyspies.spydle.matchmaker.service.MatchmakingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Responsible for delegating requests to our autoscale endpoint.
 * This endpoint is called by agones periodically to determine how many game servers we need.
 */
@RestController
public class AutoscaleController {

    @Autowired
    private MatchmakingService matchmakingService;

    @PostMapping("/autoscale")
    public Map<String, Object> autoscale(@RequestBody Map<String, Object> request) {
        return matchmakingService.autoscale(request);
    }

}