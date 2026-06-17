package br.com.insurtech.policybilling.application.usecase;

import br.com.insurtech.policybilling.application.port.out.PolicyRepositoryPort;
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
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessDailyBillingUseCaseImplTest {

    @Mock
    private PolicyRepositoryPort policyRepositoryPort;

    @InjectMocks
    private ProcessDailyBillingUseCaseImpl processDailyBillingUseCase;

    @Test
    @DisplayName("should process due policies successfully")
    void shouldProcessDuePoliciesSuccessfully() {
        // Given
        LocalDate currentDate = LocalDate.of(2026, 6, 10);
        Policy firstPolicy = buildActivePolicy(10);
        Policy secondPolicy = buildActivePolicy(10);
        when(policyRepositoryPort.findByDueDayAndStatus(10, PolicyStatus.ACTIVE))
                .thenReturn(List.of(firstPolicy, secondPolicy));

        // When
        processDailyBillingUseCase.execute(currentDate);

        // Then
        assertThat(firstPolicy.status()).isEqualTo(PolicyStatus.PENDING_PAYMENT);
        assertThat(secondPolicy.status()).isEqualTo(PolicyStatus.PENDING_PAYMENT);
        verify(policyRepositoryPort).findByDueDayAndStatus(10, PolicyStatus.ACTIVE);
        verify(policyRepositoryPort, times(2)).save(any(Policy.class));
        verify(policyRepositoryPort).save(firstPolicy);
        verify(policyRepositoryPort).save(secondPolicy);
    }

    @Test
    @DisplayName("should not process when no policies found")
    void shouldNotProcessWhenNoPoliciesFound() {
        // Given
        LocalDate currentDate = LocalDate.of(2026, 6, 10);
        when(policyRepositoryPort.findByDueDayAndStatus(10, PolicyStatus.ACTIVE))
                .thenReturn(List.of());

        // When
        processDailyBillingUseCase.execute(currentDate);

        // Then
        verify(policyRepositoryPort).findByDueDayAndStatus(10, PolicyStatus.ACTIVE);
        verify(policyRepositoryPort, never()).save(any(Policy.class));
    }

    @Test
    @DisplayName("should not process when policy is not due for billing")
    void shouldNotProcessWhenPolicyNotDueForBilling() {
        // Given
        LocalDate currentDate = LocalDate.of(2026, 6, 10);
        Policy policyNotDueForBilling = buildActivePolicy(11);
        when(policyRepositoryPort.findByDueDayAndStatus(10, PolicyStatus.ACTIVE))
                .thenReturn(List.of(policyNotDueForBilling));

        // When
        processDailyBillingUseCase.execute(currentDate);

        // Then
        assertThat(policyNotDueForBilling.status()).isEqualTo(PolicyStatus.ACTIVE);
        verify(policyRepositoryPort).findByDueDayAndStatus(10, PolicyStatus.ACTIVE);
        verify(policyRepositoryPort, never()).save(policyNotDueForBilling);
    }

    private static Policy buildActivePolicy(int dueDay) {
        return Policy.issue(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new MobileDevice("Brand", "Model", "123456789012345", new BigDecimal("499.90")),
                new BigDecimal("99.90"),
                dueDay
        );
    }
}
