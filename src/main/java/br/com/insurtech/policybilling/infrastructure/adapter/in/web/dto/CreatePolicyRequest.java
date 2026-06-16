package br.com.insurtech.policybilling.infrastructure.adapter.in.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record CreatePolicyRequest(
        @NotNull
        UUID customerId,
        @NotBlank
        String deviceBrand,
        @NotBlank
        String deviceModel,
        @NotBlank
        @Pattern(regexp = "\\d{15}")
        String deviceImei,
        @NotNull
        @Positive
        BigDecimal deviceInvoiceValue,
        @NotNull
        @Positive
        BigDecimal monthlyPremium,
        @Min(1)
        @Max(28)
        int dueDay
) {}
