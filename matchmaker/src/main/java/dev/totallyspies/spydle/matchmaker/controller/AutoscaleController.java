package dev.totallyspies.spydle.matchmaker.controller;

import dev.totallyspies.spydle.matchmaker.generated.model.AutoscaleRequestModel;
import dev.totallyspies.spydle.matchmaker.generated.model.AutoscaleResponseModel;
import dev.totallyspies.spydle.matchmaker.generated.model.AutoscaleResponseModelResponse;
import dev.totallyspies.spydle.matchmaker.use_case.AutoscaleService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Responsible for delegating requests to our autoscale endpoint.
 * This endpoint is called by agones periodically to determine how many game servers we need.
 */
@RestController
@Validated
public class AutoscaleController {

    private final Logger logger = LoggerFactory.getLogger(AutoscaleController.class);

    private AutoscaleService autoscalerService;

    public AutoscaleController(AutoscaleService autoscalerService) {
        this.autoscalerService = autoscalerService;
    }

    @PostMapping("/autoscale")
    public ResponseEntity<?> autoscale(@Valid @RequestBody AutoscaleRequestModel request) {
        logger.info("Received request: /autoscale, request: {}", request.toJson());
        try {
            int desired = autoscalerService.autoscale(request.getRequest().getStatus());
            AutoscaleResponseModelResponse response = new AutoscaleResponseModelResponse()
                    .uid(request.getRequest().getUid())
                    .replicas(desired)
                    .scale(true);
            AutoscaleResponseModel responseWrapped = new AutoscaleResponseModel().response(response);
            logger.info("Successfully handled /autoscale request, response: {}", response.toJson());
            return ResponseEntity.ok(responseWrapped);
        } catch (Exception exception) {
            logger.error("Failed to handle /autoscale", exception);
            return ResponseEntity.status(500).body(exception.getMessage());
        }
    }

}