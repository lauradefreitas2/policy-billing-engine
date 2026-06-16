package br.com.insurtech.policybilling.infrastructure.adapter.in.web.api;

import br.com.insurtech.policybilling.infrastructure.adapter.in.web.dto.CreatePolicyRequest;
import br.com.insurtech.policybilling.infrastructure.adapter.in.web.dto.PolicyResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/v1/policies")
@Tag(name = "Policies", description = "Policy management endpoints")
public interface PolicyApi {

    @PostMapping
    @Operation(summary = "Create a new mobile insurance policy")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Policy created successfully"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data (Validation error)",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Business rule violation",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected system error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = org.springframework.http.ProblemDetail.class))
            )
    })
    ResponseEntity<PolicyResponse> createPolicy(@Valid @RequestBody CreatePolicyRequest request);
}
