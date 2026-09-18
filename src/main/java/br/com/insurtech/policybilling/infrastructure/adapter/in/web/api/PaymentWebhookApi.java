package br.com.insurtech.policybilling.infrastructure.adapter.in.web.api;

import br.com.insurtech.policybilling.infrastructure.adapter.in.web.dto.PaymentWebhookRequest;
import br.com.insurtech.policybilling.infrastructure.adapter.in.web.dto.PaymentWebhookResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/v1/webhooks/payments")
@Tag(
        name = "Webhooks de pagamento",
        description = "Recebimento de confirmações de pagamento enviadas pelo provedor externo."
)
public interface PaymentWebhookApi {

    @PostMapping
    @SecurityRequirements
    @Operation(
            summary = "Confirmar pagamento de uma apólice",
            description = """
                    Processa uma notificação de pagamento bem-sucedido.

                    Apólices ACTIVE permanecem ativas. Apólices PENDING_PAYMENT ou SUSPENDED
                    são reativadas e têm a data de suspensão removida. Apólices CANCELED não
                    podem ser reativadas.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Pagamento confirmado e apólice ativa.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PaymentWebhookResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "policyId": "a3cdd876-7f2a-4bd0-a62e-a1859388657d",
                                              "policyStatus": "ACTIVE"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Payload ausente, malformado ou com status não suportado.",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Apólice não encontrada.",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "A apólice está cancelada e não pode ser reativada.",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = org.springframework.http.ProblemDetail.class)
                    )
            )
    })
    ResponseEntity<PaymentWebhookResponse> confirmPayment(
            @Valid
            @org.springframework.web.bind.annotation.RequestBody
            @RequestBody(
                    description = "Confirmação de pagamento vinculada a uma apólice.",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PaymentWebhookRequest.class),
                            examples = @ExampleObject(
                                    name = "Pagamento aprovado",
                                    value = """
                                            {
                                              "policyId": "a3cdd876-7f2a-4bd0-a62e-a1859388657d",
                                              "status": "SUCCEEDED"
                                            }
                                            """
                            )
                    )
            )
            PaymentWebhookRequest request
    );
}
