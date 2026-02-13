package com.airasia.smartloadapplication.controller;

import com.airasia.smartloadapplication.entities.OptimizeRequest;
import com.airasia.smartloadapplication.entities.OptimizeResponse;
import com.airasia.smartloadapplication.services.LoadOptimizerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/load-optimizer")
public class LoadOptimizerController {

    private final LoadOptimizerService service;

    public LoadOptimizerController(LoadOptimizerService service) {
        this.service = service;
    }

    @PostMapping("/optimize")
    public ResponseEntity<?> optimize(@RequestBody OptimizeRequest request) {

        if (request.getTruck() == null || request.getOrders() == null) {
            return ResponseEntity.badRequest().build();
        }

        if (request.getOrders().size() > 22) {
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).build();
        }

        return ResponseEntity.ok(service.optimize(request));
    }

    @GetMapping("/healthz")
    public String health() {
        return "OK";
    }
}

