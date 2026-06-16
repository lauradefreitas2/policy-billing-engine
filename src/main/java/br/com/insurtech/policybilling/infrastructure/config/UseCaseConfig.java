package br.com.insurtech.policybilling.infrastructure.config;

import br.com.insurtech.policybilling.application.port.in.CreatePolicyUseCase;
import br.com.insurtech.policybilling.application.port.out.PolicyRepositoryPort;
import br.com.insurtech.policybilling.application.usecase.CreatePolicyUseCaseImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public CreatePolicyUseCase createPolicyUseCase(PolicyRepositoryPort policyRepositoryPort) {
        return new CreatePolicyUseCaseImpl(policyRepositoryPort);
    }
}
