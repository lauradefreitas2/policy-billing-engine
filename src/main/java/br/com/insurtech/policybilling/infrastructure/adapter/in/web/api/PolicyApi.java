package br.com.insurtech.policybilling.infrastructure.adapter.in.web.api;

import br.com.insurtech.policybilling.infrastructure.adapter.in.web.dto.CreatePolicyRequest;
import br.com.insurtech.policybilling.infrastructure.adapter.in.web.dto.PolicyResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/v1/policies")
@Tag(
        name = "Apólices",
        description = "Operações para emissão e acompanhamento de apólices de seguro mobile."
)
public interface PolicyApi {

    @PostMapping
    @Operation(
            summary = "Emitir uma nova apólice mobile",
            description = """
                    Cria uma apólice de seguro para um aparelho celular.

                    Regras principais:
                    - O IMEI deve conter exatamente 15 dígitos.
                    - O valor da nota fiscal e o prêmio mensal devem ser positivos.
                    - O vencimento deve estar entre os dias 1 e 28.
                    - Toda apólice criada nasce com status ACTIVE.
                    - A cobertura inicial é NEW_DEVICE_REPLACEMENT.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Apólice emitida com sucesso.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PolicyResponse.class),
                            examples = @ExampleObject(
                                    name = "Apólice criada",
                                    value = """
                                            {
                                              "id": "4f7f593f-85b7-4db0-a4c2-2fcb9f769e13",
                                              "customerId": "7f3a7d7b-9c52-41af-9f8d-41c6fce8f924",
                                              "status": "ACTIVE"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos na requisição. Use esta resposta para corrigir campos obrigatórios, formatos e limites.",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = org.springframework.http.ProblemDetail.class),
                            examples = @ExampleObject(
                                    name = "IMEI e vencimento inválidos",
                                    value = """
                                            {
                                              "type": "about:blank",
                                              "title": "Bad Request",
                                              "status": 400,
                                              "detail": "Validation failed",
                                              "instance": "/api/v1/policies",
                                              "invalid_params": [
                                                {
                                                  "field": "deviceImei",
                                                  "message": "must match \\"\\\\d{15}\\""
                                                },
                                                {
                                                  "field": "dueDay",
                                                  "message": "must be less than or equal to 28"
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Violação de regra de negócio do domínio.",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = org.springframework.http.ProblemDetail.class),
                            examples = @ExampleObject(
                                    name = "Regra de domínio violada",
                                    value = """
                                            {
                                              "type": "about:blank",
                                              "title": "Unprocessable Entity",
                                              "status": 422,
                                              "detail": "monthlyPremium must be greater than zero",
                                              "instance": "/api/v1/policies"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro inesperado no servidor.",
                    content = @Content(
                            mediaType = "application/problem+json",
                            schema = @Schema(implementation = org.springframework.http.ProblemDetail.class),
                            examples = @ExampleObject(
                                    name = "Erro inesperado",
                                    value = """
                                            {
                                              "type": "about:blank",
                                              "title": "Internal Server Error",
                                              "status": 500,
                                              "detail": "Unexpected system error",
                                              "instance": "/api/v1/policies"
                                            }
                                            """
                            )
                    )
            )
    })
    ResponseEntity<PolicyResponse> createPolicy(
            @Valid
            @org.springframework.web.bind.annotation.RequestBody
            @RequestBody(
                    description = "Payload para emissão de uma nova apólice mobile.",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreatePolicyRequest.class),
                            examples = @ExampleObject(
                                    name = "Apólice para iPhone 15",
                                    summary = "Exemplo válido para testar rapidamente",
                                    value = """
                                            {
                                              "customerId": "7f3a7d7b-9c52-41af-9f8d-41c6fce8f924",
                                              "deviceBrand": "Apple",
                                              "deviceModel": "iPhone 15",
                                              "deviceImei": "123456789012345",
                                              "deviceInvoiceValue": 5999.90,
                                              "monthlyPremium": 99.90,
                                              "dueDay": 10
                                            }
                                            """
                            )
                    )
            )
            CreatePolicyRequest request
    );
}
