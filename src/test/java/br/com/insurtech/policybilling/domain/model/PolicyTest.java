package br.com.insurtech.policybilling.domain.model;

import br.com.insurtech.policybilling.domain.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class PolicyTest {

    @Test
    @DisplayName("should create policy when all fields are valid")
    void shouldCreatePolicyWhenAllFieldsAreValid() {
        Policy policy = createValidPolicy(1, PolicyStatus.ACTIVE);

        assertThat(policy).isNotNull();
        assertThat(policy.id()).isNotNull();
        assertThat(policy.customerId()).isNotNull();
        assertThat(policy.device()).isNotNull();
        assertThat(policy.coverage()).isEqualTo(CoverageType.NEW_DEVICE_REPLACEMENT);
        assertThat(policy.monthlyPremium()).isEqualByComparingTo("99.90");
        assertThat(policy.dueDay()).isEqualTo(1);
        assertThat(policy.status()).isEqualTo(PolicyStatus.ACTIVE);
    }

    @Test
    @DisplayName("should create policy when due day is at lower boundary")
    void shouldCreatePolicyWhenDueDayAtLowerBoundary() {
        Policy policy = createValidPolicy(1, PolicyStatus.ACTIVE);

        assertThat(policy.dueDay()).isEqualTo(1);
    }

    @Test
    @DisplayName("should create policy when due day is at upper boundary")
    void shouldCreatePolicyWhenDueDayAtUpperBoundary() {
        Policy policy = createValidPolicy(28, PolicyStatus.ACTIVE);

        assertThat(policy.dueDay()).isEqualTo(28);
    }

    @Test
    @DisplayName("should throw DomainException when due day is below lower boundary")
    void shouldThrowExceptionWhenDueDayIsInvalidBelowLowerBoundary() {
        assertThatThrownBy(() -> createValidPolicy(0, PolicyStatus.ACTIVE))
                .isInstanceOf(DomainException.class)
                .hasMessage("dueDay must be between 1 and 28");
    }

    @Test
    @DisplayName("should throw DomainException when due day is above upper boundary")
    void shouldThrowExceptionWhenDueDayIsInvalidAboveUpperBoundary() {
        assertThatThrownBy(() -> createValidPolicy(29, PolicyStatus.ACTIVE))
                .isInstanceOf(DomainException.class)
                .hasMessage("dueDay must be between 1 and 28");
    }

    @Test
    @DisplayName("should return true when policy is active and due date matches")
    void shouldReturnTrueWhenPolicyIsActiveAndDueDateMatches() {
        Policy policy = createValidPolicy(15, PolicyStatus.ACTIVE);
        LocalDate billingDate = LocalDate.of(2026, 6, 15);

        assertThat(policy.isDueForBilling(billingDate)).isTrue();
    }

    @Test
    @DisplayName("should return false when policy is pending payment even if due date matches")
    void shouldReturnFalseWhenPolicyIsPendingPaymentEvenIfDueDateMatches() {
        Policy policy = createValidPolicy(15, PolicyStatus.PENDING_PAYMENT);
        LocalDate billingDate = LocalDate.of(2026, 6, 15);

        assertThat(policy.isDueForBilling(billingDate)).isFalse();
    }

    @Test
    @DisplayName("should return false when policy is canceled even if due date matches")
    void shouldReturnFalseWhenPolicyIsCanceledEvenIfDueDateMatches() {
        Policy policy = createValidPolicy(15, PolicyStatus.CANCELED);
        LocalDate billingDate = LocalDate.of(2026, 6, 15);

        assertThat(policy.isDueForBilling(billingDate)).isFalse();
    }

    @Test
    @DisplayName("should mark policy as pending payment from active")
    void shouldMarkPolicyAsPendingPaymentFromActive() {
        Policy policy = createValidPolicy(10, PolicyStatus.ACTIVE);

        policy.markAsPendingPayment();

        assertThat(policy.status()).isEqualTo(PolicyStatus.PENDING_PAYMENT);
    }

    @Test
    @DisplayName("should not mark canceled policy as pending payment")
    void shouldNotMarkCanceledPolicyAsPendingPayment() {
        Policy policy = createValidPolicy(10, PolicyStatus.CANCELED);

        assertThatThrownBy(policy::markAsPendingPayment)
                .isInstanceOf(DomainException.class)
                .hasMessage("Canceled policies cannot be marked as pending payment");
    }

    @Test
    @DisplayName("should cancel policy from active")
    void shouldCancelPolicyFromActive() {
        Policy policy = createValidPolicy(20, PolicyStatus.ACTIVE);

        policy.cancel();

        assertThat(policy.status()).isEqualTo(PolicyStatus.CANCELED);
    }

    @Test
    @DisplayName("should cancel policy from pending payment")
    void shouldCancelPolicyFromPendingPayment() {
        Policy policy = createValidPolicy(20, PolicyStatus.PENDING_PAYMENT);

        policy.cancel();

        assertThat(policy.status()).isEqualTo(PolicyStatus.CANCELED);
    }

    private static Policy createValidPolicy(int dueDay, PolicyStatus status) {
        return new Policy(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new MobileDevice("TestBrand", "TestModel", "123456789012345", new BigDecimal("399.99")),
                CoverageType.NEW_DEVICE_REPLACEMENT,
                new BigDecimal("99.90"),
                dueDay,
                status
        );
    }
}
