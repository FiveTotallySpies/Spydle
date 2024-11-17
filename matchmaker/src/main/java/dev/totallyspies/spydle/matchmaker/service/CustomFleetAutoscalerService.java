package dev.totallyspies.spydle.matchmaker.service;

import dev.totallyspies.spydle.matchmaker.generated.model.AutoscaleRequestModelRequestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CustomFleetAutoscalerService {

    private final Logger logger = LoggerFactory.getLogger(CustomFleetAutoscalerService.class);

    public int autoscale(AutoscaleRequestModelRequestStatus currentState) {
        // TODO configure based on env
        int allocatedReplicas = currentState.getAllocatedReplicas();
        int desiredIdleReplicas = (int) (allocatedReplicas * 0.5);
        desiredIdleReplicas = Math.max(4, desiredIdleReplicas);
        desiredIdleReplicas = Math.min(10, desiredIdleReplicas);
        int desiredReplicas = allocatedReplicas + desiredIdleReplicas;
        logger.debug("Calculated desired replicas for autoscale target: {}", desiredReplicas);
        return desiredReplicas;
    }

}
