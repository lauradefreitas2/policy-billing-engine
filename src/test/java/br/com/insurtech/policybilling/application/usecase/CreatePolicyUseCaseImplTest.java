package br.com.insurtech.policybilling.application.usecase;

import br.com.insurtech.policybilling.application.port.in.CreatePolicyCommand;
import br.com.insurtech.policybilling.application.port.out.PolicyRepositoryPort;
import br.com.insurtech.policybilling.domain.exception.DomainException;
import br.com.insurtech.policybilling.domain.model.CoverageType;
import br.com.insurtech.policybilling.domain.model.Policy;
import br.com.insurtech.policybilling.domain.model.PolicyStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreatePolicyUseCaseImplTest {

    @Mock
    private PolicyRepositoryPort policyRepositoryPort;

    @InjectMocks
    private CreatePolicyUseCaseImpl createPolicyUseCase;

    @Test
    @DisplayName("should issue active policy successfully")
    void shouldCreatePolicySuccessfully() {
        CreatePolicyCommand command = buildValidCommand();
        when(policyRepositoryPort.save(any(Policy.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Policy result = createPolicyUseCase.execute(command);

        assertThat(result.id()).isNotNull();
        assertThat(result.customerId()).isEqualTo(command.customerId());
        assertThat(result.device().brand()).isEqualTo(command.deviceBrand());
        assertThat(result.device().model()).isEqualTo(command.deviceModel());
        assertThat(result.device().imei()).isEqualTo(command.deviceImei());
        assertThat(result.device().invoiceValue()).isEqualByComparingTo(command.deviceInvoiceValue());
        assertThat(result.coverage()).isEqualTo(CoverageType.NEW_DEVICE_REPLACEMENT);
        assertThat(result.monthlyPremium()).isEqualByComparingTo(command.monthlyPremium());
        assertThat(result.dueDay()).isEqualTo(command.dueDay());
        assertThat(result.status()).isEqualTo(PolicyStatus.ACTIVE);
        verify(policyRepositoryPort).save(result);
    }

    @Test
    @DisplayName("should throw exception when command is null")
    void shouldThrowExceptionWhenCommandIsNull() {
        assertThatThrownBy(() -> createPolicyUseCase.execute(null))
                .isInstanceOf(DomainException.class)
                .hasMessage("create policy command must not be null");

        verifyNoInteractions(policyRepositoryPort);
    }

    @Test
    @DisplayName("should not save policy when command violates domain rules")
    void shouldNotSavePolicyWhenCommandViolatesDomainRules() {
        CreatePolicyCommand invalidCommand = new CreatePolicyCommand(
                UUID.randomUUID(),
                "Brand",
                "Model",
                "123456789012345",
                new BigDecimal("499.90"),
                new BigDecimal("99.90"),
                29
        );

        assertThatThrownBy(() -> createPolicyUseCase.execute(invalidCommand))
                .isInstanceOf(DomainException.class)
                .hasMessage("dueDay must be between 1 and 28");

        verifyNoInteractions(policyRepositoryPort);
    }

    private static CreatePolicyCommand buildValidCommand() {
        return new CreatePolicyCommand(
                UUID.randomUUID(),
                "Brand",
                "Model",
                "123456789012345",
                new BigDecimal("499.90"),
                new BigDecimal("99.90"),
                10
        );
    }

    @Test
    @DisplayName("should throw exception when instantiating with null repository")
    void shouldThrowExceptionWhenRepositoryIsNull() {
        assertThatThrownBy(() -> new CreatePolicyUseCaseImpl(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("policyRepositoryPort must not be null");
    }
}
