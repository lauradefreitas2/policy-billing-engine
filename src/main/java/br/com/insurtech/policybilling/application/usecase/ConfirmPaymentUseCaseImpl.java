package br.com.insurtech.policybilling.application.usecase;

import br.com.insurtech.policybilling.application.exception.PolicyNotFoundException;
import br.com.insurtech.policybilling.application.port.in.ConfirmPaymentCommand;
import br.com.insurtech.policybilling.application.port.in.ConfirmPaymentUseCase;
import br.com.insurtech.policybilling.application.port.out.PolicyRepositoryPort;
import br.com.insurtech.policybilling.domain.exception.DomainException;
import br.com.insurtech.policybilling.domain.model.Policy;

import java.util.Objects;

public class ConfirmPaymentUseCaseImpl implements ConfirmPaymentUseCase {

    private final PolicyRepositoryPort policyRepositoryPort;

    public ConfirmPaymentUseCaseImpl(PolicyRepositoryPort policyRepositoryPort) {
        this.policyRepositoryPort = Objects.requireNonNull(
                policyRepositoryPort,
                "policyRepositoryPort must not be null"
        );
    }

    @Override
    public Policy execute(ConfirmPaymentCommand command) {
        if (command == null) {
            throw new DomainException("confirm payment command must not be null");
        }
        if (command.policyId() == null) {
            throw new DomainException("policyId must not be null");
        }
        if (command.status() == null) {
            throw new DomainException("payment status must not be null");
        }

        Policy policy = policyRepositoryPort.findById(command.policyId())
                .orElseThrow(() -> new PolicyNotFoundException(command.policyId()));

        policy.confirmPayment();

        return policyRepositoryPort.save(policy);
    }
}
