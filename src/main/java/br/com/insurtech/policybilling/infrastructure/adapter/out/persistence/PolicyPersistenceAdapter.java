package br.com.insurtech.policybilling.infrastructure.adapter.out.persistence;

import br.com.insurtech.policybilling.application.port.out.PolicyRepositoryPort;
import br.com.insurtech.policybilling.domain.model.Policy;
import br.com.insurtech.policybilling.domain.model.PolicyStatus;
import br.com.insurtech.policybilling.infrastructure.adapter.out.persistence.entity.PolicyEntity;
import br.com.insurtech.policybilling.infrastructure.adapter.out.persistence.mapper.PolicyMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Component
public class PolicyPersistenceAdapter implements PolicyRepositoryPort {

    private final SpringDataPolicyRepository policyRepository;

    public PolicyPersistenceAdapter(SpringDataPolicyRepository policyRepository) {
        this.policyRepository = Objects.requireNonNull(policyRepository, "policyRepository must not be null");
    }

    @Override
    @SuppressWarnings("null")
    public Policy save(Policy policy) {
        PolicyEntity entity = PolicyMapper.toEntity(policy);
        PolicyEntity savedEntity = policyRepository.save(entity);
        return PolicyMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Policy> findById(UUID id) {
        Objects.requireNonNull(id, "id must not be null");
        return policyRepository.findById(id)
                .map(PolicyMapper::toDomain);
    }

    @Override
    public List<Policy> findByDueDayAndStatus(int dueDay, PolicyStatus status) {
        Objects.requireNonNull(status, "status must not be null");
        return policyRepository.findByDueDayAndStatus(dueDay, status.name())
                .stream()
                .map(PolicyMapper::toDomain)
                .toList();
    }
}
