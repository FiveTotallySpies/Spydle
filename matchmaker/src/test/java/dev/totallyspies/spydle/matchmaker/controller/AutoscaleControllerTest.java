package dev.totallyspies.spydle.matchmaker.controller;

import dev.totallyspies.spydle.matchmaker.generated.model.AutoscaleRequestModel;
import dev.totallyspies.spydle.matchmaker.generated.model.AutoscaleRequestModelRequest;
import dev.totallyspies.spydle.matchmaker.generated.model.AutoscaleRequestModelRequestStatus;
import dev.totallyspies.spydle.matchmaker.generated.model.AutoscaleResponseModel;
import dev.totallyspies.spydle.matchmaker.use_case.AutoscaleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AutoscaleControllerTest {

    private AutoscaleController autoscaleController;

    @Mock
    private AutoscaleService autoscalerService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        autoscaleController = new AutoscaleController(autoscalerService);
    }

    @Test
    public void testAutoscale_Success() {
        AutoscaleRequestModel request = new AutoscaleRequestModel();
        AutoscaleRequestModelRequest requestInner = new AutoscaleRequestModelRequest();
        requestInner.setUid("unique-id");
        AutoscaleRequestModelRequestStatus status = new AutoscaleRequestModelRequestStatus();
        status.setAllocatedReplicas(5);
        status.setReplicas(10);
        status.setReadyReplicas(5);
        requestInner.setStatus(status);
        request.setRequest(requestInner);

        when(autoscalerService.autoscale(status)).thenReturn(7);

        ResponseEntity<?> response = autoscaleController.autoscale(request);

        assertEquals(200, response.getStatusCode().value());
        AutoscaleResponseModel responseBody = (AutoscaleResponseModel) response.getBody();
        assertNotNull(responseBody);
        assertEquals("unique-id", responseBody.getResponse().getUid());
        assertEquals(7, responseBody.getResponse().getReplicas());
        assertTrue(responseBody.getResponse().getScale());
    }

    @Test
    public void testAutoscale_Exception() {
        AutoscaleRequestModel request = new AutoscaleRequestModel();

        when(autoscalerService.autoscale(any())).thenThrow(new RuntimeException("Error"));

        ResponseEntity<?> response = autoscaleController.autoscale(request);

        assertEquals(500, response.getStatusCode().value());
    }

}
