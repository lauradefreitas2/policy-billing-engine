package br.com.insurtech.policybilling.application.port.in;

import java.math.BigDecimal;
import java.util.UUID;

public record CreatePolicyCommand(
        UUID customerId,
        String deviceBrand,
        String deviceModel,
        String deviceImei,
        BigDecimal deviceInvoiceValue,
        BigDecimal monthlyPremium,
        int dueDay
) {}
