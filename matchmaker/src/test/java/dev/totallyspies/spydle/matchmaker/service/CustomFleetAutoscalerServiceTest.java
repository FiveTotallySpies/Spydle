package dev.totallyspies.spydle.matchmaker.service;

import dev.totallyspies.spydle.matchmaker.generated.model.AutoscaleRequestModelRequestStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CustomFleetAutoscalerServiceTest {

    private CustomFleetAutoscalerService autoscalerService;

    @BeforeEach
    public void setUp() {
        autoscalerService = new CustomFleetAutoscalerService();
    }

    @Test
    public void testAutoscale() {
        AutoscaleRequestModelRequestStatus status = new AutoscaleRequestModelRequestStatus();
        status.setAllocatedReplicas(10);
        status.setReplicas(15);
        status.setReadyReplicas(5);

        int desiredReplicas = autoscalerService.autoscale(status);

        assertEquals(15, desiredReplicas);
    }

    @Test
    public void testAutoscale_MinimumReplicas() {
        AutoscaleRequestModelRequestStatus status = new AutoscaleRequestModelRequestStatus();
        status.setAllocatedReplicas(0);
        status.setReplicas(4);
        status.setReadyReplicas(4);

        int desiredReplicas = autoscalerService.autoscale(status);

        assertEquals(4, desiredReplicas);
    }

    @Test
    public void testAutoscale_ScaleDownReplicas() {
        AutoscaleRequestModelRequestStatus status = new AutoscaleRequestModelRequestStatus();
        status.setAllocatedReplicas(0);
        status.setReplicas(10);
        status.setReadyReplicas(10);

        int desiredReplicas = autoscalerService.autoscale(status);

        assertEquals(4, desiredReplicas);
    }

    @Test
    public void testAutoscale_ScaleUpReplicas() {
        AutoscaleRequestModelRequestStatus status = new AutoscaleRequestModelRequestStatus();
        status.setAllocatedReplicas(5);
        status.setReplicas(6);
        status.setReadyReplicas(5);

        int desiredReplicas = autoscalerService.autoscale(status);

        assertEquals(9, desiredReplicas);
    }

    @Test
    public void testAutoscale_MaximumReplicas() {
        AutoscaleRequestModelRequestStatus status = new AutoscaleRequestModelRequestStatus();
        status.setAllocatedReplicas(20);
        status.setReplicas(30);
        status.setReadyReplicas(10);

        int desiredReplicas = autoscalerService.autoscale(status);

        assertEquals(30, desiredReplicas);
    }

}