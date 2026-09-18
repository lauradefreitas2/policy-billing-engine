package br.com.insurtech.policybilling.application.port.in;

import java.util.UUID;

public record ConfirmPaymentCommand(
        UUID policyId,
        SuccessfulPaymentStatus status
) {}
