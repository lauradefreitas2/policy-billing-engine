package br.com.insurtech.policybilling.infrastructure.adapter.in.web.dto;

import br.com.insurtech.policybilling.domain.model.PolicyStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Resumo da apólice criada.")
public record PolicyResponse(
        @Schema(
                description = "Identificador único da apólice emitida.",
                example = "4f7f593f-85b7-4db0-a4c2-2fcb9f769e13"
        )
        UUID id,

        @Schema(
                description = "Identificador do cliente vinculado à apólice.",
                example = "7f3a7d7b-9c52-41af-9f8d-41c6fce8f924"
        )
        UUID customerId,

        @Schema(
                description = "Status inicial da apólice. Toda apólice recém emitida nasce como ACTIVE.",
                example = "ACTIVE",
                allowableValues = {"ACTIVE", "PENDING_PAYMENT", "CANCELED"}
        )
        PolicyStatus status
) {}
