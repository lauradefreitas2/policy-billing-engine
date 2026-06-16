package br.com.insurtech.policybilling.application.port.out;

import br.com.insurtech.policybilling.domain.model.Policy;

import java.util.Optional;
import java.util.UUID;

public interface PolicyRepositoryPort {

    Policy save(Policy policy);

    Optional<Policy> findById(UUID id);
}
