package br.com.insurtech.policybilling.infrastructure.config;

import br.com.insurtech.policybilling.application.port.in.CreatePolicyUseCase;
import br.com.insurtech.policybilling.application.port.in.CancelOverduePoliciesUseCase;
import br.com.insurtech.policybilling.application.port.in.ProcessDailyBillingUseCase;
import br.com.insurtech.policybilling.application.port.in.SuspendOverduePoliciesUseCase;
import br.com.insurtech.policybilling.application.port.out.PolicyEventPublisherPort;
import br.com.insurtech.policybilling.application.port.out.PolicyRepositoryPort;
import br.com.insurtech.policybilling.application.usecase.CancelOverduePoliciesUseCaseImpl;
import br.com.insurtech.policybilling.application.usecase.CreatePolicyUseCaseImpl;
import br.com.insurtech.policybilling.application.usecase.ProcessDailyBillingUseCaseImpl;
import br.com.insurtech.policybilling.application.usecase.SuspendOverduePoliciesUseCaseImpl;
import br.com.insurtech.policybilling.infrastructure.observability.ObservedCreatePolicyUseCase;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
            PolicyEventPublisherPort policyEventPublisherPort
    ) {
        return new CancelOverduePoliciesUseCaseImpl(policyRepositoryPort, policyEventPublisherPort);
    }
}
