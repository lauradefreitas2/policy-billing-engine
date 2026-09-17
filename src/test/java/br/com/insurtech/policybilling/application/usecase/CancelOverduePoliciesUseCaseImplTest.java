package br.com.insurtech.policybilling.application.usecase;

import br.com.insurtech.policybilling.application.port.out.PolicyCanceledEvent;
import br.com.insurtech.policybilling.application.port.out.PolicyEventPublisherPort;
import br.com.insurtech.policybilling.application.port.out.PolicyRepositoryPort;
import br.com.insurtech.policybilling.domain.model.CoverageType;
import br.com.insurtech.policybilling.domain.model.MobileDevice;
import br.com.insurtech.policybilling.domain.model.Policy;
import br.com.insurtech.policybilling.domain.model.PolicyStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class CancelOverduePoliciesUseCaseImplTest {

    @Mock
    private PolicyRepositoryPort policyRepositoryPort;

    @Mock
    private PolicyEventPublisherPort policyEventPublisherPort;

    @InjectMocks
    private CancelOverduePoliciesUseCaseImpl cancelOverduePoliciesUseCase;

    @Test
    @DisplayName("should cancel policies overdue by at least ten days")
    void shouldCancelPoliciesOverdueByAtLeastTenDays() {
        // Given
        LocalDate currentDate = LocalDate.of(2026, 6, 20);
        Policy firstPolicy = buildPendingPaymentPolicy(10);
        Policy secondPolicy = buildPendingPaymentPolicy(10);
        when(policyRepositoryPort.findByStatus(PolicyStatus.PENDING_PAYMENT))
                .thenReturn(List.of(firstPolicy, secondPolicy));

        // When
        cancelOverduePoliciesUseCase.execute(currentDate);

        // Then
        assertThat(firstPolicy.status()).isEqualTo(PolicyStatus.CANCELED);
        assertThat(secondPolicy.status()).isEqualTo(PolicyStatus.CANCELED);
        verify(policyRepositoryPort).findByStatus(PolicyStatus.PENDING_PAYMENT);
        verify(policyRepositoryPort, times(2)).save(any(Policy.class));
        verify(policyRepositoryPort).save(firstPolicy);
        verify(policyRepositoryPort).save(secondPolicy);
        verify(policyEventPublisherPort, times(2)).publishPolicyCanceledEvent(any(PolicyCanceledEvent.class));
    }

    @Test
    @DisplayName("should publish policy canceled event after saving canceled policy")
    void shouldPublishPolicyCanceledEventAfterSavingCanceledPolicy() {
        // Given
        LocalDate currentDate = LocalDate.of(2026, 6, 20);
        Policy policy = buildPendingPaymentPolicy(10);
        when(policyRepositoryPort.findByStatus(PolicyStatus.PENDING_PAYMENT))
                .thenReturn(List.of(policy));

        // When
        cancelOverduePoliciesUseCase.execute(currentDate);

        // Then
        ArgumentCaptor<PolicyCanceledEvent> eventCaptor = ArgumentCaptor.forClass(PolicyCanceledEvent.class);
        verify(policyRepositoryPort).save(policy);
        verify(policyEventPublisherPort).publishPolicyCanceledEvent(eventCaptor.capture());

        PolicyCanceledEvent event = eventCaptor.getValue();
        assertThat(event.policyId()).isEqualTo(policy.id());
        assertThat(event.customerId()).isEqualTo(policy.customerId());
        assertThat(event.canceledAt()).isNotNull();
    }

    @Test
    @DisplayName("should not cancel policies overdue by less than ten days")
    void shouldNotCancelPoliciesOverdueByLessThanTenDays() {
        // Given
        LocalDate currentDate = LocalDate.of(2026, 6, 19);
        Policy policy = buildPendingPaymentPolicy(10);
        when(policyRepositoryPort.findByStatus(PolicyStatus.PENDING_PAYMENT))
                .thenReturn(List.of(policy));

        // When
        cancelOverduePoliciesUseCase.execute(currentDate);

        // Then
        assertThat(policy.status()).isEqualTo(PolicyStatus.PENDING_PAYMENT);
        verify(policyRepositoryPort).findByStatus(PolicyStatus.PENDING_PAYMENT);
        verify(policyRepositoryPort, never()).save(policy);
        verify(policyEventPublisherPort, never()).publishPolicyCanceledEvent(any(PolicyCanceledEvent.class));
    }

    @Test
    @DisplayName("should calculate overdue days across month boundary")
    void shouldCalculateOverdueDaysAcrossMonthBoundary() {
        // Given
        LocalDate currentDate = LocalDate.of(2026, 7, 8);
        Policy policy = buildPendingPaymentPolicy(28);
        when(policyRepositoryPort.findByStatus(PolicyStatus.PENDING_PAYMENT))
                .thenReturn(List.of(policy));

        // When
        cancelOverduePoliciesUseCase.execute(currentDate);

        // Then
        assertThat(policy.status()).isEqualTo(PolicyStatus.CANCELED);
        verify(policyRepositoryPort).findByStatus(PolicyStatus.PENDING_PAYMENT);
        verify(policyRepositoryPort).save(policy);
        verify(policyEventPublisherPort).publishPolicyCanceledEvent(any(PolicyCanceledEvent.class));
    }

    @Test
    @DisplayName("should finish gracefully when no pending policies are found")
    void shouldFinishGracefullyWhenNoPendingPoliciesAreFound() {
        // Given
        LocalDate currentDate = LocalDate.of(2026, 6, 20);
        when(policyRepositoryPort.findByStatus(PolicyStatus.PENDING_PAYMENT))
                .thenReturn(List.of());

        // When
        cancelOverduePoliciesUseCase.execute(currentDate);

        // Then
        verify(policyRepositoryPort).findByStatus(PolicyStatus.PENDING_PAYMENT);
        verify(policyRepositoryPort, never()).save(any(Policy.class));
        verify(policyEventPublisherPort, never()).publishPolicyCanceledEvent(any(PolicyCanceledEvent.class));
    }

    private static Policy buildPendingPaymentPolicy(int dueDay) {
        return new Policy(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new MobileDevice("Brand", "Model", "123456789012345", new BigDecimal("499.90")),
                CoverageType.NEW_DEVICE_REPLACEMENT,
                new BigDecimal("99.90"),
                dueDay,
                PolicyStatus.PENDING_PAYMENT
        );
    }
}
