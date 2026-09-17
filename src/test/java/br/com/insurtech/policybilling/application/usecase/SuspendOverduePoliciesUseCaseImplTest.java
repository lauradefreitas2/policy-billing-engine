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
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SuspendOverduePoliciesUseCaseImplTest {

    @Mock
    private PolicyRepositoryPort policyRepositoryPort;

    @InjectMocks
    private SuspendOverduePoliciesUseCaseImpl suspendOverduePoliciesUseCase;

    @Test
    @DisplayName("should suspend active and pending policies when payment is at least one day overdue")
    void shouldSuspendActiveAndPendingPoliciesWhenPaymentIsAtLeastOneDayOverdue() {
        LocalDate currentDate = LocalDate.of(2026, 6, 11);
        Policy activePolicy = TestFixtures.activePolicy(UUID.randomUUID(), UUID.randomUUID(), 10);
        Policy pendingPolicy = TestFixtures.policyWithStatus(UUID.randomUUID(), UUID.randomUUID(), 10, PolicyStatus.PENDING_PAYMENT);
        when(policyRepositoryPort.findByStatus(PolicyStatus.ACTIVE)).thenReturn(List.of(activePolicy));
        when(policyRepositoryPort.findByStatus(PolicyStatus.PENDING_PAYMENT)).thenReturn(List.of(pendingPolicy));

        suspendOverduePoliciesUseCase.execute(currentDate);

        assertThat(activePolicy.status()).isEqualTo(PolicyStatus.SUSPENDED);
        assertThat(pendingPolicy.status()).isEqualTo(PolicyStatus.SUSPENDED);
        assertThat(activePolicy.suspendedAt()).isEqualTo(LocalDateTime.of(2026, 6, 11, 0, 0));
        assertThat(pendingPolicy.suspendedAt()).isEqualTo(LocalDateTime.of(2026, 6, 11, 0, 0));

        ArgumentCaptor<Policy> savedPolicies = ArgumentCaptor.forClass(Policy.class);
        verify(policyRepositoryPort, times(2)).save(savedPolicies.capture());
        assertThat(savedPolicies.getAllValues())
                .extracting(Policy::status)
                .containsExactly(PolicyStatus.SUSPENDED, PolicyStatus.SUSPENDED);
    }

    @Test
    @DisplayName("should not suspend policies on due date")
    void shouldNotSuspendPoliciesOnDueDate() {
        LocalDate currentDate = LocalDate.of(2026, 6, 10);
        Policy activePolicy = TestFixtures.activePolicy(UUID.randomUUID(), UUID.randomUUID(), 10);
        when(policyRepositoryPort.findByStatus(PolicyStatus.ACTIVE)).thenReturn(List.of(activePolicy));
        when(policyRepositoryPort.findByStatus(PolicyStatus.PENDING_PAYMENT)).thenReturn(List.of());

        suspendOverduePoliciesUseCase.execute(currentDate);

        assertThat(activePolicy.status()).isEqualTo(PolicyStatus.ACTIVE);
        verify(policyRepositoryPort, never()).save(any(Policy.class));
    }

    @Test
    @DisplayName("should calculate overdue days across month boundary")
    void shouldCalculateOverdueDaysAcrossMonthBoundary() {
        LocalDate currentDate = LocalDate.of(2026, 7, 1);
        Policy pendingPolicy = TestFixtures.policyWithStatus(UUID.randomUUID(), UUID.randomUUID(), 28, PolicyStatus.PENDING_PAYMENT);
        when(policyRepositoryPort.findByStatus(PolicyStatus.ACTIVE)).thenReturn(List.of());
        when(policyRepositoryPort.findByStatus(PolicyStatus.PENDING_PAYMENT)).thenReturn(List.of(pendingPolicy));

        suspendOverduePoliciesUseCase.execute(currentDate);

        assertThat(pendingPolicy.status()).isEqualTo(PolicyStatus.SUSPENDED);
        assertThat(pendingPolicy.suspendedAt()).isEqualTo(LocalDateTime.of(2026, 7, 1, 0, 0));
        verify(policyRepositoryPort).save(pendingPolicy);
    }

    @Test
    @DisplayName("should reject null current date before hitting repository")
    void shouldRejectNullCurrentDateBeforeHittingRepository() {
        assertThatThrownBy(() -> suspendOverduePoliciesUseCase.execute(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("currentDate must not be null");

        verifyNoInteractions(policyRepositoryPort);
    }

    @Test
    @DisplayName("should throw exception when instantiating with null repository")
    void shouldThrowExceptionWhenRepositoryIsNull() {
        assertThatThrownBy(() -> new SuspendOverduePoliciesUseCaseImpl(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("policyRepositoryPort must not be null");
    }
}
