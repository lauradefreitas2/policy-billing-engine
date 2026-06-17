package br.com.insurtech.policybilling.application.port.out;

import br.com.insurtech.policybilling.domain.model.Policy;
import br.com.insurtech.policybilling.domain.model.PolicyStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PolicyRepositoryPort {

    Policy save(Policy policy);

    Optional<Policy> findById(UUID id);

    List<Policy> findByDueDayAndStatus(int dueDay, PolicyStatus status);
}
