package br.com.insurtech.policybilling.application.usecase;

import br.com.insurtech.policybilling.application.port.in.CreatePolicyCommand;
import br.com.insurtech.policybilling.application.port.in.CreatePolicyUseCase;
import br.com.insurtech.policybilling.application.port.out.PolicyRepositoryPort;
import br.com.insurtech.policybilling.domain.exception.DomainException;
import br.com.insurtech.policybilling.domain.model.MobileDevice;
import br.com.insurtech.policybilling.domain.model.Policy;

import java.util.Objects;
import java.util.UUID;

public class CreatePolicyUseCaseImpl implements CreatePolicyUseCase {

    private final PolicyRepositoryPort policyRepositoryPort;

    public CreatePolicyUseCaseImpl(PolicyRepositoryPort policyRepositoryPort) {
        this.policyRepositoryPort = Objects.requireNonNull(
                policyRepositoryPort,
                "policyRepositoryPort must not be null"
        );
    }

    @Override
    public Policy execute(CreatePolicyCommand command) {
        if (command == null) {
            throw new DomainException("create policy command must not be null");
        }

        MobileDevice device = new MobileDevice(
                command.deviceBrand(),
                command.deviceModel(),
                command.deviceImei(),
                command.deviceInvoiceValue()
        );

        Policy policy = Policy.issue(
                UUID.randomUUID(),
                command.customerId(),
                device,
                command.monthlyPremium(),
                command.dueDay()
        );

        return policyRepositoryPort.save(policy);
    }
}
