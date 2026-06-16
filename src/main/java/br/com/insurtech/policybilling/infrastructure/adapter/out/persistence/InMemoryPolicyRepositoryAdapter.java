package br.com.insurtech.policybilling.infrastructure.adapter.out.persistence;

import br.com.insurtech.policybilling.application.port.out.PolicyRepositoryPort;
import br.com.insurtech.policybilling.domain.model.Policy;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryPolicyRepositoryAdapter implements PolicyRepositoryPort {

    private final Map<UUID, Policy> policies = new ConcurrentHashMap<>();

    @Override
    public Policy save(Policy policy) {
        Objects.requireNonNull(policy, "policy must not be null");
        policies.put(policy.id(), policy);
        return policy;
    }

    @Override
    public Optional<Policy> findById(UUID id) {
        Objects.requireNonNull(id, "id must not be null");
        return Optional.ofNullable(policies.get(id));
    }
}
