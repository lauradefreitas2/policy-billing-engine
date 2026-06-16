package br.com.insurtech.policybilling.infrastructure.adapter.in.web.dto;

import br.com.insurtech.policybilling.domain.model.PolicyStatus;

import java.util.UUID;

public record PolicyResponse(
        UUID id,
        UUID customerId,
        PolicyStatus status
) {}
