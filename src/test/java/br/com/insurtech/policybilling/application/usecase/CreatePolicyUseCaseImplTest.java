package br.com.insurtech.policybilling.application.usecase;

import br.com.insurtech.policybilling.application.port.out.PolicyRepositoryPort;
import br.com.insurtech.policybilling.domain.exception.DomainException;
import br.com.insurtech.policybilling.domain.model.CoverageType;
import br.com.insurtech.policybilling.domain.model.MobileDevice;
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
    @DisplayName("should create policy successfully")
    void shouldCreatePolicySuccessfully() {
        Policy validPolicy = buildValidPolicy();
        when(policyRepositoryPort.save(any(Policy.class))).thenReturn(validPolicy);

        Policy result = createPolicyUseCase.execute(validPolicy);

        assertThat(result).isSameAs(validPolicy);
        verify(policyRepositoryPort).save(validPolicy);
    }

    @Test
    @DisplayName("should throw exception when policy is null")
    void shouldThrowExceptionWhenPolicyIsNull() {
        assertThatThrownBy(() -> createPolicyUseCase.execute(null))
                .isInstanceOf(DomainException.class)
                .hasMessage("policy must not be null");

        verifyNoInteractions(policyRepositoryPort);
    }

    private static Policy buildValidPolicy() {
        return new Policy(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new MobileDevice("Brand", "Model", "123456789012345", new BigDecimal("499.90")),
                CoverageType.NEW_DEVICE_REPLACEMENT,
                new BigDecimal("99.90"),
                10,
                PolicyStatus.ACTIVE
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
