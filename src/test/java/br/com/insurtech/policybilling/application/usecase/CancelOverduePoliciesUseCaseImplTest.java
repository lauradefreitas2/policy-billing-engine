package br.com.insurtech.policybilling.application.usecase;

import br.com.insurtech.policybilling.TestFixtures;
import br.com.insurtech.policybilling.application.port.out.PolicyCanceledEvent;
import br.com.insurtech.policybilling.application.port.out.PolicyEventOutboxPort;
import br.com.insurtech.policybilling.application.port.out.PolicyRepositoryPort;
import br.com.insurtech.policybilling.domain.model.Policy;
import br.com.insurtech.policybilling.domain.model.PolicyStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CancelOverduePoliciesUseCaseImplTest {

    private static final Instant EVENT_TIME = Instant.parse("2026-06-21T12:00:00Z");

    @Mock
    private PolicyRepositoryPort policyRepositoryPort;

    @Mock
    private PolicyEventOutboxPort policyEventOutboxPort;

    private CancelOverduePoliciesUseCaseImpl cancelOverduePoliciesUseCase;

    @BeforeEach
    void setUp() {
        cancelOverduePoliciesUseCase = new CancelOverduePoliciesUseCaseImpl(
                policyRepositoryPort,
                policyEventOutboxPort,
                Clock.fixed(EVENT_TIME, ZoneOffset.UTC)
        );
    }

    @Test
    @DisplayName("should cancel policies suspended for at least ten days")
    void shouldCancelPoliciesSuspendedForAtLeastTenDays() {
        LocalDate currentDate = LocalDate.of(2026, 6, 21);
        Policy firstPolicy = buildSuspendedPolicy(LocalDateTime.of(2026, 6, 11, 0, 0));
        Policy secondPolicy = buildSuspendedPolicy(LocalDateTime.of(2026, 6, 10, 0, 0));
        when(policyRepositoryPort.findByStatus(PolicyStatus.SUSPENDED))
                .thenReturn(List.of(firstPolicy, secondPolicy));

        cancelOverduePoliciesUseCase.execute(currentDate);

        assertThat(firstPolicy.status()).isEqualTo(PolicyStatus.CANCELED);
        assertThat(secondPolicy.status()).isEqualTo(PolicyStatus.CANCELED);
        verify(policyRepositoryPort).findByStatus(PolicyStatus.SUSPENDED);
        verify(policyRepositoryPort, times(2)).save(any(Policy.class));
        verify(policyRepositoryPort).save(firstPolicy);
        verify(policyRepositoryPort).save(secondPolicy);
        verify(policyEventOutboxPort, times(2)).appendPolicyCanceledEvent(any(PolicyCanceledEvent.class));
    }

    @Test
    @DisplayName("should publish policy canceled event after saving canceled policy")
    void shouldPublishPolicyCanceledEventAfterSavingCanceledPolicy() {
        LocalDate currentDate = LocalDate.of(2026, 6, 21);
        Policy policy = buildSuspendedPolicy(LocalDateTime.of(2026, 6, 11, 0, 0));
        when(policyRepositoryPort.findByStatus(PolicyStatus.SUSPENDED))
                .thenReturn(List.of(policy));

        cancelOverduePoliciesUseCase.execute(currentDate);

        ArgumentCaptor<PolicyCanceledEvent> eventCaptor = ArgumentCaptor.forClass(PolicyCanceledEvent.class);
        InOrder inOrder = inOrder(policyRepositoryPort, policyEventOutboxPort);
        inOrder.verify(policyRepositoryPort).save(policy);
        inOrder.verify(policyEventOutboxPort).appendPolicyCanceledEvent(eventCaptor.capture());

        PolicyCanceledEvent event = eventCaptor.getValue();
        assertThat(event.eventId()).isNotNull();
        assertThat(event.policyId()).isEqualTo(policy.id());
        assertThat(event.customerId()).isEqualTo(policy.customerId());
        assertThat(event.canceledAt()).isEqualTo(EVENT_TIME);
    }

    @Test
    @DisplayName("should cancel policy exactly on tenth suspended day")
    void shouldCancelPolicyExactlyOnTenthSuspendedDay() {
        LocalDate currentDate = LocalDate.of(2026, 6, 21);
        Policy policy = buildSuspendedPolicy(LocalDateTime.of(2026, 6, 11, 0, 0));
        when(policyRepositoryPort.findByStatus(PolicyStatus.SUSPENDED))
                .thenReturn(List.of(policy));

        cancelOverduePoliciesUseCase.execute(currentDate);

        assertThat(policy.status()).isEqualTo(PolicyStatus.CANCELED);
        verify(policyRepositoryPort).save(policy);
        verify(policyEventOutboxPort).appendPolicyCanceledEvent(any(PolicyCanceledEvent.class));
    }

    @Test
    @DisplayName("should not cancel policies suspended for nine days")
    void shouldNotCancelPoliciesSuspendedForNineDays() {
        LocalDate currentDate = LocalDate.of(2026, 6, 20);
        Policy policy = buildSuspendedPolicy(LocalDateTime.of(2026, 6, 11, 0, 0));
        when(policyRepositoryPort.findByStatus(PolicyStatus.SUSPENDED))
                .thenReturn(List.of(policy));

        cancelOverduePoliciesUseCase.execute(currentDate);

        assertThat(policy.status()).isEqualTo(PolicyStatus.SUSPENDED);
        verify(policyRepositoryPort).findByStatus(PolicyStatus.SUSPENDED);
        verify(policyRepositoryPort, never()).save(policy);
        verify(policyEventOutboxPort, never()).appendPolicyCanceledEvent(any(PolicyCanceledEvent.class));
    }

    @Test
    @DisplayName("should calculate suspended days across month boundary")
    void shouldCalculateSuspendedDaysAcrossMonthBoundary() {
        LocalDate currentDate = LocalDate.of(2026, 7, 8);
        Policy policy = buildSuspendedPolicy(LocalDateTime.of(2026, 6, 28, 0, 0));
        when(policyRepositoryPort.findByStatus(PolicyStatus.SUSPENDED))
                .thenReturn(List.of(policy));

        cancelOverduePoliciesUseCase.execute(currentDate);

        assertThat(policy.status()).isEqualTo(PolicyStatus.CANCELED);
        verify(policyRepositoryPort).findByStatus(PolicyStatus.SUSPENDED);
        verify(policyRepositoryPort).save(policy);
        verify(policyEventOutboxPort).appendPolicyCanceledEvent(any(PolicyCanceledEvent.class));
    }

    @Test
    @DisplayName("should finish gracefully when no suspended policies are found")
    void shouldFinishGracefullyWhenNoSuspendedPoliciesAreFound() {
        LocalDate currentDate = LocalDate.of(2026, 6, 20);
        when(policyRepositoryPort.findByStatus(PolicyStatus.SUSPENDED))
                .thenReturn(List.of());

        cancelOverduePoliciesUseCase.execute(currentDate);

        verify(policyRepositoryPort).findByStatus(PolicyStatus.SUSPENDED);
        verify(policyRepositoryPort, never()).save(any(Policy.class));
        verify(policyEventOutboxPort, never()).appendPolicyCanceledEvent(any(PolicyCanceledEvent.class));
    }

    @Test
    @DisplayName("should reject null current date before hitting repository")
    void shouldRejectNullCurrentDateBeforeHittingRepository() {
        assertThatThrownBy(() -> cancelOverduePoliciesUseCase.execute(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("currentDate must not be null");

        verifyNoInteractions(policyRepositoryPort, policyEventOutboxPort);
    }

    @Test
    @DisplayName("should throw exception when instantiating with null repository")
    void shouldThrowExceptionWhenRepositoryIsNull() {
        assertThatThrownBy(() -> new CancelOverduePoliciesUseCaseImpl(
                null,
                policyEventOutboxPort,
                Clock.systemUTC()
        ))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("policyRepositoryPort must not be null");
    }

    @Test
    @DisplayName("should throw exception when instantiating with null publisher")
    void shouldThrowExceptionWhenOutboxIsNull() {
        assertThatThrownBy(() -> new CancelOverduePoliciesUseCaseImpl(
                policyRepositoryPort,
                null,
                Clock.systemUTC()
        ))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("policyEventOutboxPort must not be null");
    }

    @Test
    @DisplayName("should throw exception when instantiating with null clock")
    void shouldThrowExceptionWhenClockIsNull() {
        assertThatThrownBy(() -> new CancelOverduePoliciesUseCaseImpl(
                policyRepositoryPort,
                policyEventOutboxPort,
                null
        ))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("clock must not be null");
    }

    private static Policy buildSuspendedPolicy(LocalDateTime suspendedAt) {
        return new Policy(
                UUID.randomUUID(),
                UUID.randomUUID(),
                TestFixtures.validMobileDevice(),
                br.com.insurtech.policybilling.domain.model.CoverageType.NEW_DEVICE_REPLACEMENT,
                TestFixtures.MONTHLY_PREMIUM,
                TestFixtures.DUE_DAY,
                PolicyStatus.SUSPENDED,
                suspendedAt
        );
    }
}
