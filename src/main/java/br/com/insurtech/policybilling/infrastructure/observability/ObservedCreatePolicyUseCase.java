package br.com.insurtech.policybilling.infrastructure.observability;

import br.com.insurtech.policybilling.application.port.in.CreatePolicyCommand;
import br.com.insurtech.policybilling.application.port.in.CreatePolicyUseCase;
import br.com.insurtech.policybilling.domain.model.Policy;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

import java.util.Objects;

public class ObservedCreatePolicyUseCase implements CreatePolicyUseCase {

    private final CreatePolicyUseCase delegate;
    private final Counter policiesCreatedCounter;

    public ObservedCreatePolicyUseCase(CreatePolicyUseCase delegate, MeterRegistry meterRegistry) {
        this.delegate = Objects.requireNonNull(delegate, "delegate must not be null");
        Objects.requireNonNull(meterRegistry, "meterRegistry must not be null");
        this.policiesCreatedCounter = Counter.builder("policies.created")
                .description("Total number of successfully created policies")
                .baseUnit("policies")
                .register(meterRegistry);
    }

    @Override
    public Policy execute(CreatePolicyCommand command) {
        Policy policy = delegate.execute(command);
        policiesCreatedCounter.increment();
        return policy;
    }
}
