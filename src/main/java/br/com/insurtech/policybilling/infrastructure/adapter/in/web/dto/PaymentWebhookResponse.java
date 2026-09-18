package br.com.insurtech.policybilling.infrastructure.adapter.in.web.dto;

import br.com.insurtech.policybilling.domain.model.PolicyStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Resultado da confirmação de pagamento da apólice.")
public record PaymentWebhookResponse(
        @Schema(example = "a3cdd876-7f2a-4bd0-a62e-a1859388657d")
        UUID policyId,

        @Schema(example = "ACTIVE")
        PolicyStatus policyStatus
) {}
