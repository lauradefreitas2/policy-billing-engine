package br.com.insurtech.policybilling.infrastructure.adapter.in.web.controller;

import br.com.insurtech.policybilling.application.port.in.CreatePolicyCommand;
import br.com.insurtech.policybilling.application.port.in.CreatePolicyUseCase;
import br.com.insurtech.policybilling.domain.model.Policy;
import br.com.insurtech.policybilling.infrastructure.adapter.in.web.dto.CreatePolicyRequest;
import br.com.insurtech.policybilling.infrastructure.adapter.in.web.dto.PolicyResponse;
import br.com.insurtech.policybilling.infrastructure.adapter.in.web.api.PolicyApi;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/policies")
public class PolicyController implements PolicyApi {

    private final CreatePolicyUseCase createPolicyUseCase;

    public PolicyController(CreatePolicyUseCase createPolicyUseCase) {
        this.createPolicyUseCase = createPolicyUseCase;
    }

    @Override
    public ResponseEntity<PolicyResponse> createPolicy(@Valid @RequestBody CreatePolicyRequest request) {
        CreatePolicyCommand command = new CreatePolicyCommand(
                request.customerId(),
                request.deviceBrand(),
                request.deviceModel(),
                request.deviceImei(),
                request.deviceInvoiceValue(),
                request.monthlyPremium(),
                request.dueDay()
        );

        Policy saved = createPolicyUseCase.execute(command);

        PolicyResponse response = new PolicyResponse(saved.id(), saved.customerId(), saved.status());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
