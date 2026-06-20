package br.com.insurtech.policybilling.infrastructure.observability;

import br.com.insurtech.policybilling.application.port.in.CreatePolicyCommand;
import br.com.insurtech.policybilling.application.port.in.CreatePolicyUseCase;
import br.com.insurtech.policybilling.domain.exception.DomainException;
import br.com.insurtech.policybilling.domain.model.MobileDevice;
import br.com.insurtech.policybilling.domain.model.Policy;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ObservedCreatePolicyUseCaseTest {

    private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
    private final CreatePolicyUseCase delegate = mock(CreatePolicyUseCase.class);
    private final ObservedCreatePolicyUseCase useCase = new ObservedCreatePolicyUseCase(delegate, meterRegistry);

    @Test
    @DisplayName("should increment policies created counter after successful creation")
    void shouldIncrementPoliciesCreatedCounterAfterSuccessfulCreation() {
        CreatePolicyCommand command = buildCommand();
        when(delegate.execute(command)).thenReturn(buildPolicy(command));

        useCase.execute(command);

        assertThat(meterRegistry.counter("policies.created").count()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("should not increment policies created counter when creation fails")
    void shouldNotIncrementPoliciesCreatedCounterWhenCreationFails() {
        CreatePolicyCommand command = buildCommand();
        when(delegate.execute(command)).thenThrow(new DomainException("policy could not be created"));

        assertThatThrownBy(() -> useCase.execute(command))
                .isInstanceOf(DomainException.class)
                .hasMessage("policy could not be created");
        assertThat(meterRegistry.counter("policies.created").count()).isZero();
    }

    private static CreatePolicyCommand buildCommand() {
        return new CreatePolicyCommand(
                UUID.randomUUID(),
                "Apple",
                "iPhone 15",
                "123456789012345",
                new BigDecimal("5999.90"),
                new BigDecimal("99.90"),
                10
        );
    }

    private static Policy buildPolicy(CreatePolicyCommand command) {
        return Policy.issue(
                UUID.randomUUID(),
                command.customerId(),
                new MobileDevice(
                        command.deviceBrand(),
                        command.deviceModel(),
                        command.deviceImei(),
                        command.deviceInvoiceValue()
                ),
                command.monthlyPremium(),
                command.dueDay()
        );
    }
}
