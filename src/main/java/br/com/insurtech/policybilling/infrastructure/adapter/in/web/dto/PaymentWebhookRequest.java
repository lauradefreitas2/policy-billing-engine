package br.com.insurtech.policybilling.infrastructure.adapter.in.web.dto;

import br.com.insurtech.policybilling.application.port.in.SuccessfulPaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Notificação de pagamento confirmado enviada pelo provedor de pagamentos.")
public record PaymentWebhookRequest(
        @Schema(
                description = "Identificador da apólice associada ao pagamento.",
                example = "a3cdd876-7f2a-4bd0-a62e-a1859388657d",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull
        UUID policyId,

        @Schema(
                description = "Status de sucesso informado pelo provedor.",
                example = "SUCCEEDED",
                allowableValues = {"PAID", "SUCCEEDED"},
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull
        SuccessfulPaymentStatus status
) {}
