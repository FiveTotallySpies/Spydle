package dev.totallyspies.spydle.matchmaker.controller;

import dev.totallyspies.spydle.matchmaker.generated.model.AutoscaleRequestModel;
import dev.totallyspies.spydle.matchmaker.generated.model.AutoscaleResponseModel;
import dev.totallyspies.spydle.matchmaker.service.MatchmakingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Responsible for delegating requests to our autoscale endpoint.
 * This endpoint is called by agones periodically to determine how many game servers we need.
 */
@RestController
public class AutoscaleController {

    @Autowired
    private MatchmakingService matchmakingService;

    @PostMapping("/autoscale")
    public ResponseEntity<AutoscaleResponseModel> autoscale(@RequestBody AutoscaleRequestModel request) {
        return ResponseEntity.ok(matchmakingService.autoscale(request));
    }

}