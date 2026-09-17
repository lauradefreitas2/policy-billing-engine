package br.com.insurtech.policybilling.infrastructure.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Dados necessários para emitir uma apólice de seguro mobile.")
public record CreatePolicyRequest(
        @Schema(
                description = "Identificador do cliente contratante da apólice.",
                example = "7f3a7d7b-9c52-41af-9f8d-41c6fce8f924",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull
        UUID customerId,

        @Schema(
                description = "Marca do aparelho segurado.",
                example = "Apple",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank
        String deviceBrand,

        @Schema(
                description = "Modelo comercial do aparelho segurado.",
                example = "iPhone 15",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank
        String deviceModel,

        @Schema(
                description = "IMEI do aparelho. Deve conter exatamente 15 dígitos numéricos.",
                example = "123456789012345",
                minLength = 15,
                maxLength = 15,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank
        @Pattern(regexp = "\\d{15}")
        String deviceImei,

        @Schema(
                description = "Valor da nota fiscal do aparelho. Deve ser maior que zero.",
                example = "5999.90",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull
        @Positive
        BigDecimal deviceInvoiceValue,

        @Schema(
                description = "Valor mensal do prêmio do seguro. Deve ser maior que zero.",
                example = "99.90",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull
        @Positive
        BigDecimal monthlyPremium,

        @Schema(
                description = "Dia de vencimento mensal da apólice. Permitido apenas entre 1 e 28 para evitar problemas em meses curtos.",
                example = "10",
                minimum = "1",
                maximum = "28",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @Min(1)
        @Max(28)
        int dueDay
) {}
