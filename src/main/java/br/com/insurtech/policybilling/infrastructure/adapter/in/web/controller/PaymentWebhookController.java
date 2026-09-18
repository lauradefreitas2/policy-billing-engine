package br.com.insurtech.policybilling.infrastructure.adapter.in.web.controller;

import br.com.insurtech.policybilling.application.port.in.ConfirmPaymentCommand;
import br.com.insurtech.policybilling.application.port.in.ConfirmPaymentUseCase;
import br.com.insurtech.policybilling.domain.model.Policy;
import br.com.insurtech.policybilling.infrastructure.adapter.in.web.api.PaymentWebhookApi;
import br.com.insurtech.policybilling.infrastructure.adapter.in.web.dto.PaymentWebhookRequest;
import br.com.insurtech.policybilling.infrastructure.adapter.in.web.dto.PaymentWebhookResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/webhooks/payments")
public class PaymentWebhookController implements PaymentWebhookApi {

    private final ConfirmPaymentUseCase confirmPaymentUseCase;

    public PaymentWebhookController(ConfirmPaymentUseCase confirmPaymentUseCase) {
        this.confirmPaymentUseCase = confirmPaymentUseCase;
    }

    @Override
    public ResponseEntity<PaymentWebhookResponse> confirmPayment(
            @Valid @RequestBody PaymentWebhookRequest request
    ) {
        ConfirmPaymentCommand command = new ConfirmPaymentCommand(request.policyId(), request.status());
        Policy policy = confirmPaymentUseCase.execute(command);

        return ResponseEntity.ok(new PaymentWebhookResponse(policy.id(), policy.status()));
    }
}
