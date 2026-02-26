package com.kratosgado.blog.backend.controllers.v1;

import com.kratosgado.blog.backend.services.MetricsService;
import com.kratosgado.blog.dtos.response.MetricsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/metrics")
@RequiredArgsConstructor
@Tag(name = "Metrics", description = "System runtime metrics")
@SecurityRequirement(name = "Bearer Authentication")
public class MetricsController {

  private final MetricsService metricsService;

  @Operation(summary = "Get system metrics history")
  @GetMapping
  public ResponseEntity<MetricsResponse> getMetrics() {
    return ResponseEntity.ok(metricsService.getMetrics());
  }
}
