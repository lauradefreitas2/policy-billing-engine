package br.com.insurtech.policybilling.application.port.out;

import java.time.LocalDateTime;
import java.util.UUID;

public record PolicyCanceledEvent(
        UUID policyId,
        UUID customerId,
        LocalDateTime canceledAt
) {}
