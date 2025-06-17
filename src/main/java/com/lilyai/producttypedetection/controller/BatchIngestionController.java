package com.lilyai.producttypedetection.controller;

import com.lilyai.producttypedetection.dto.ProductRequest;
import com.lilyai.producttypedetection.dto.BatchIngestionResult;
import com.lilyai.producttypedetection.service.BatchIngestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@RestController
@RequestMapping("/api")
@Validated
public class BatchIngestionController {

    private final BatchIngestionService batchIngestionService;

    public BatchIngestionController(BatchIngestionService batchIngestionService) {
        this.batchIngestionService = batchIngestionService;
    }

    @PostMapping("/batch-ingest")
    public ResponseEntity<BatchIngestionResult> batchIngest(
            @Valid @RequestBody @NotEmpty List<@Valid ProductRequest> productRequests) {
        
        var result = batchIngestionService.processBatch(productRequests);
        return ResponseEntity.ok(result);
    }
}