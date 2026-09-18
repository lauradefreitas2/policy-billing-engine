package br.com.insurtech.policybilling.infrastructure.config;

import br.com.insurtech.policybilling.application.port.in.CreatePolicyUseCase;
import br.com.insurtech.policybilling.application.port.in.CancelOverduePoliciesUseCase;
import br.com.insurtech.policybilling.application.port.in.ProcessDailyBillingUseCase;
import br.com.insurtech.policybilling.application.port.in.SuspendOverduePoliciesUseCase;
import br.com.insurtech.policybilling.application.port.out.PolicyEventOutboxPort;
import br.com.insurtech.policybilling.application.port.out.PolicyRepositoryPort;
import br.com.insurtech.policybilling.application.usecase.CancelOverduePoliciesUseCaseImpl;
import br.com.insurtech.policybilling.application.usecase.CreatePolicyUseCaseImpl;
import br.com.insurtech.policybilling.application.usecase.ProcessDailyBillingUseCaseImpl;
import br.com.insurtech.policybilling.application.usecase.SuspendOverduePoliciesUseCaseImpl;
import br.com.insurtech.policybilling.infrastructure.observability.ObservedCreatePolicyUseCase;
import br.com.insurtech.policybilling.infrastructure.transaction.TransactionalCancelOverduePoliciesUseCase;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class UseCaseConfig {

    @Bean
    public CreatePolicyUseCase createPolicyUseCase(
            PolicyRepositoryPort policyRepositoryPort,
            MeterRegistry meterRegistry
    ) {
        CreatePolicyUseCase createPolicyUseCase = new CreatePolicyUseCaseImpl(policyRepositoryPort);
        return new ObservedCreatePolicyUseCase(createPolicyUseCase, meterRegistry);
    }

    @Bean
    public ProcessDailyBillingUseCase processDailyBillingUseCase(PolicyRepositoryPort policyRepositoryPort) {
        return new ProcessDailyBillingUseCaseImpl(policyRepositoryPort);
    }

    @Bean
    public SuspendOverduePoliciesUseCase suspendOverduePoliciesUseCase(PolicyRepositoryPort policyRepositoryPort) {
        return new SuspendOverduePoliciesUseCaseImpl(policyRepositoryPort);
    }

    @Bean
    public CancelOverduePoliciesUseCase cancelOverduePoliciesUseCase(
            PolicyRepositoryPort policyRepositoryPort,
            PolicyEventOutboxPort policyEventOutboxPort,
            Clock clock
    ) {
        CancelOverduePoliciesUseCase useCase = new CancelOverduePoliciesUseCaseImpl(
                policyRepositoryPort,
                policyEventOutboxPort,
                clock
        );
        return new TransactionalCancelOverduePoliciesUseCase(useCase);
    }
}
