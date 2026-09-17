package br.com.insurtech.policybilling.application.usecase;

import br.com.insurtech.policybilling.TestFixtures;
import br.com.insurtech.policybilling.application.port.out.PolicyRepositoryPort;
import br.com.insurtech.policybilling.domain.model.Policy;
import br.com.insurtech.policybilling.domain.model.PolicyStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
        LocalDate currentDate = LocalDate.of(2026, 6, 10);
        Policy firstPolicy = TestFixtures.activePolicy();
        Policy secondPolicy = TestFixtures.activePolicy(java.util.UUID.randomUUID(), java.util.UUID.randomUUID(), 10);
        when(policyRepositoryPort.findByDueDayAndStatus(10, PolicyStatus.ACTIVE))
                .thenReturn(List.of(firstPolicy, secondPolicy));

        processDailyBillingUseCase.execute(currentDate);

        assertThat(firstPolicy.status()).isEqualTo(PolicyStatus.PENDING_PAYMENT);
        assertThat(secondPolicy.status()).isEqualTo(PolicyStatus.PENDING_PAYMENT);
        verify(policyRepositoryPort).findByDueDayAndStatus(10, PolicyStatus.ACTIVE);
        verify(policyRepositoryPort, times(2)).save(any(Policy.class));

        ArgumentCaptor<Policy> savedPolicies = ArgumentCaptor.forClass(Policy.class);
        verify(policyRepositoryPort, times(2)).save(savedPolicies.capture());
        assertThat(savedPolicies.getAllValues())
                .extracting(Policy::status)
                .containsExactly(PolicyStatus.PENDING_PAYMENT, PolicyStatus.PENDING_PAYMENT);
    }

    @Test
    @DisplayName("should not process when no policies found")
    void shouldNotProcessWhenNoPoliciesFound() {
        LocalDate currentDate = LocalDate.of(2026, 6, 10);
        when(policyRepositoryPort.findByDueDayAndStatus(10, PolicyStatus.ACTIVE))
                .thenReturn(List.of());

        processDailyBillingUseCase.execute(currentDate);

        verify(policyRepositoryPort).findByDueDayAndStatus(10, PolicyStatus.ACTIVE);
        verify(policyRepositoryPort, never()).save(any(Policy.class));
    }

    @Test
    @DisplayName("should not process when policy is not due for billing")
    void shouldNotProcessWhenPolicyNotDueForBilling() {
        LocalDate currentDate = LocalDate.of(2026, 6, 10);
        Policy policyNotDueForBilling = TestFixtures.activePolicy(java.util.UUID.randomUUID(), java.util.UUID.randomUUID(), 11);
        when(policyRepositoryPort.findByDueDayAndStatus(10, PolicyStatus.ACTIVE))
                .thenReturn(List.of(policyNotDueForBilling));

        processDailyBillingUseCase.execute(currentDate);

        assertThat(policyNotDueForBilling.status()).isEqualTo(PolicyStatus.ACTIVE);
        verify(policyRepositoryPort).findByDueDayAndStatus(10, PolicyStatus.ACTIVE);
        verify(policyRepositoryPort, never()).save(policyNotDueForBilling);
    }

    @Test
    @DisplayName("should reject null current date before hitting repository")
    void shouldRejectNullCurrentDateBeforeHittingRepository() {
        assertThatThrownBy(() -> processDailyBillingUseCase.execute(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("currentDate must not be null");

        verifyNoInteractions(policyRepositoryPort);
    }

    @Test
    @DisplayName("should throw exception when instantiating with null repository")
    void shouldThrowExceptionWhenRepositoryIsNull() {
        assertThatThrownBy(() -> new ProcessDailyBillingUseCaseImpl(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("policyRepositoryPort must not be null");
    }
}
