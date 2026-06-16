package br.com.insurtech.policybilling.application.usecase;

import br.com.insurtech.policybilling.application.port.in.CreatePolicyUseCase;
import br.com.insurtech.policybilling.application.port.out.PolicyRepositoryPort;
import br.com.insurtech.policybilling.domain.exception.DomainException;
import br.com.insurtech.policybilling.domain.model.Policy;

import java.util.Objects;

public class CreatePolicyUseCaseImpl implements CreatePolicyUseCase {

    private final PolicyRepositoryPort policyRepositoryPort;

    public CreatePolicyUseCaseImpl(PolicyRepositoryPort policyRepositoryPort) {
        this.policyRepositoryPort = Objects.requireNonNull(
                policyRepositoryPort,
                "policyRepositoryPort must not be null"
        );
    }

    @Override
    public Policy execute(Policy policy) {
        if (policy == null) {
            throw new DomainException("policy must not be null");
        }

        return policyRepositoryPort.save(policy);
    }
}
