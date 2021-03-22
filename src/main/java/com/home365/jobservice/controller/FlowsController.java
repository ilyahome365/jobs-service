package com.home365.jobservice.controller;

import com.home365.jobservice.exception.GeneralException;
import com.home365.jobservice.flow.PropertyPhasingOutFlow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class FlowsController {
    private final PropertyPhasingOutFlow propertyPhasingOutFlow;

    public FlowsController(PropertyPhasingOutFlow propertyPhasingOutFlow) {
        this.propertyPhasingOutFlow = propertyPhasingOutFlow;
    }


    @GetMapping("/move-property-toReadyForDeactivation")
    public ResponseEntity<Void> movePropertyToReadyForDeactivation(@RequestParam String propertyId) throws GeneralException {
        log.info("move property to ready for deactivation : {} ", propertyId);
        propertyPhasingOutFlow.startPropertyPhasingOut(propertyId);
        return ResponseEntity.ok().build();
    }
}
