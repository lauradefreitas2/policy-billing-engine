package br.com.insurtech.policybilling.application.port.out;

import java.time.Instant;
import java.util.UUID;

public record PolicyCanceledEvent(
        UUID eventId,
        UUID policyId,
        UUID customerId,
        Instant canceledAt
) {}
