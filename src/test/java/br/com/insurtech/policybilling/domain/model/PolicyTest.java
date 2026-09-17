package br.com.insurtech.policybilling.domain.model;

import br.com.insurtech.policybilling.domain.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        assertThat(policy.suspendedAt()).isNull();
    }

    @Test
    @DisplayName("should issue policy with default mobile insurance rules")
    void shouldIssuePolicyWithDefaultMobileInsuranceRules() {
        UUID id = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        MobileDevice device = new MobileDevice("TestBrand", "TestModel", "123456789012345", new BigDecimal("399.99"));

        Policy policy = Policy.issue(id, customerId, device, new BigDecimal("99.90"), 10);

        assertThat(policy.id()).isEqualTo(id);
        assertThat(policy.customerId()).isEqualTo(customerId);
        assertThat(policy.device()).isEqualTo(device);
        assertThat(policy.coverage()).isEqualTo(CoverageType.NEW_DEVICE_REPLACEMENT);
        assertThat(policy.status()).isEqualTo(PolicyStatus.ACTIVE);
        assertThat(policy.monthlyPremium()).isEqualByComparingTo("99.90");
        assertThat(policy.dueDay()).isEqualTo(10);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 28})
    @DisplayName("should accept due day boundaries")
    void shouldAcceptDueDayBoundaries(int dueDay) {
        Policy policy = createValidPolicy(dueDay, PolicyStatus.ACTIVE);

        assertThat(policy.dueDay()).isEqualTo(dueDay);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 29})
    @DisplayName("should reject due day outside allowed range")
    void shouldRejectDueDayOutsideAllowedRange(int dueDay) {
        assertThatThrownBy(() -> createValidPolicy(dueDay, PolicyStatus.ACTIVE))
                .isInstanceOf(DomainException.class)
                .hasMessage("dueDay must be between 1 and 28");
    }

    @ParameterizedTest
    @CsvSource({
            "ACTIVE,15,true",
            "ACTIVE,16,false",
            "PENDING_PAYMENT,15,false",
            "SUSPENDED,15,false",
            "CANCELED,15,false"
    })
    @DisplayName("should identify whether policy is due for billing based on status and day")
    void shouldIdentifyWhetherPolicyIsDueForBillingBasedOnStatusAndDay(
            PolicyStatus status,
            int billingDay,
            boolean expectedResult
    ) {
        Policy policy = createValidPolicy(15, status);
        LocalDate billingDate = LocalDate.of(2026, 6, billingDay);

        assertThat(policy.isDueForBilling(billingDate)).isEqualTo(expectedResult);
    }

    @Test
    @DisplayName("should mark policy as pending payment from active")
    void shouldMarkPolicyAsPendingPaymentFromActive() {
        Policy policy = createValidPolicy(10, PolicyStatus.ACTIVE);

        policy.markAsPendingPayment();

        assertThat(policy.status()).isEqualTo(PolicyStatus.PENDING_PAYMENT);
        assertThat(policy.suspendedAt()).isNull();
    }

    @Test
    @DisplayName("should activate pending policy after payment confirmation")
    void shouldActivatePendingPolicyAfterPaymentConfirmation() {
        Policy policy = createValidPolicy(10, PolicyStatus.PENDING_PAYMENT);

        policy.confirmPayment();

        assertThat(policy.status()).isEqualTo(PolicyStatus.ACTIVE);
    }

    @Test
    @DisplayName("should keep active policy active after payment confirmation")
    void shouldKeepActivePolicyActiveAfterPaymentConfirmation() {
        Policy policy = createValidPolicy(10, PolicyStatus.ACTIVE);

        policy.confirmPayment();

        assertThat(policy.status()).isEqualTo(PolicyStatus.ACTIVE);
    }

    @Test
    @DisplayName("should activate suspended policy after payment confirmation and clear suspension date")
    void shouldActivateSuspendedPolicyAfterPaymentConfirmationAndClearSuspensionDate() {
        Policy policy = createValidPolicy(10, PolicyStatus.SUSPENDED);

        policy.confirmPayment();

        assertThat(policy.status()).isEqualTo(PolicyStatus.ACTIVE);
        assertThat(policy.suspendedAt()).isNull();
    }

    @Test
    @DisplayName("should not activate canceled policy after payment confirmation")
    void shouldNotActivateCanceledPolicyAfterPaymentConfirmation() {
        Policy policy = createValidPolicy(10, PolicyStatus.CANCELED);

        assertThatThrownBy(policy::confirmPayment)
                .isInstanceOf(DomainException.class)
                .hasMessage("Canceled policies cannot be activated after payment confirmation");
    }

    @Test
    @DisplayName("should not mark canceled policy as pending payment")
    void shouldNotMarkCanceledPolicyAsPendingPayment() {
        Policy policy = createValidPolicy(10, PolicyStatus.CANCELED);

        assertThatThrownBy(policy::markAsPendingPayment)
                .isInstanceOf(DomainException.class)
                .hasMessage("Canceled or suspended policies cannot be marked as pending payment");
    }

    @Test
    @DisplayName("should not mark suspended policy as pending payment")
    void shouldNotMarkSuspendedPolicyAsPendingPayment() {
        Policy policy = createValidPolicy(10, PolicyStatus.SUSPENDED);

        assertThatThrownBy(policy::markAsPendingPayment)
                .isInstanceOf(DomainException.class)
                .hasMessage("Canceled or suspended policies cannot be marked as pending payment");
    }

    @ParameterizedTest
    @EnumSource(value = PolicyStatus.class, names = {"ACTIVE", "PENDING_PAYMENT", "SUSPENDED", "CANCELED"})
    @DisplayName("should move any status to canceled when cancellation is generic")
    void shouldMoveAnyStatusToCanceledWhenCancellationIsGeneric(PolicyStatus initialStatus) {
        Policy policy = createValidPolicy(20, initialStatus);

        policy.cancel();

        assertThat(policy.status()).isEqualTo(PolicyStatus.CANCELED);
    }

    @Test
    @DisplayName("should suspend active policy due to non payment")
    void shouldSuspendActivePolicyDueToNonPayment() {
        Policy policy = createValidPolicy(20, PolicyStatus.ACTIVE);
        LocalDateTime suspendedAt = LocalDateTime.of(2026, 6, 21, 0, 0);

        policy.suspendDueToNonPayment(suspendedAt);

        assertThat(policy.status()).isEqualTo(PolicyStatus.SUSPENDED);
        assertThat(policy.suspendedAt()).isEqualTo(suspendedAt);
    }

    @Test
    @DisplayName("should suspend pending payment policy due to non payment")
    void shouldSuspendPendingPaymentPolicyDueToNonPayment() {
        Policy policy = createValidPolicy(20, PolicyStatus.PENDING_PAYMENT);
        LocalDateTime suspendedAt = LocalDateTime.of(2026, 6, 21, 0, 0);

        policy.suspendDueToNonPayment(suspendedAt);

        assertThat(policy.status()).isEqualTo(PolicyStatus.SUSPENDED);
        assertThat(policy.suspendedAt()).isEqualTo(suspendedAt);
    }

    @Test
    @DisplayName("should reject suspension without suspension date")
    void shouldRejectSuspensionWithoutSuspensionDate() {
        Policy policy = createValidPolicy(20, PolicyStatus.PENDING_PAYMENT);

        assertThatThrownBy(() -> policy.suspendDueToNonPayment(null))
                .isInstanceOf(DomainException.class)
                .hasMessage("suspendedAt must not be null");
    }

    @Test
    @DisplayName("should reject suspended policy without suspension date")
    void shouldRejectSuspendedPolicyWithoutSuspensionDate() {
        assertThatThrownBy(() -> new Policy(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new MobileDevice("TestBrand", "TestModel", "123456789012345", new BigDecimal("399.99")),
                CoverageType.NEW_DEVICE_REPLACEMENT,
                new BigDecimal("99.90"),
                20,
                PolicyStatus.SUSPENDED
        ))
                .isInstanceOf(DomainException.class)
                .hasMessage("suspendedAt must not be null when policy is suspended");
    }

    @Test
    @DisplayName("should cancel suspended policy due to non payment")
    void shouldCancelSuspendedPolicyDueToNonPayment() {
        Policy policy = createValidPolicy(20, PolicyStatus.SUSPENDED);

        policy.cancelDueToNonPayment();

        assertThat(policy.status()).isEqualTo(PolicyStatus.CANCELED);
        assertThat(policy.suspendedAt()).isNotNull();
    }

    @Test
    @DisplayName("should not cancel non suspended policy due to non payment")
    void shouldNotCancelNonSuspendedPolicyDueToNonPayment() {
        Policy policy = createValidPolicy(20, PolicyStatus.PENDING_PAYMENT);

        assertThatThrownBy(policy::cancelDueToNonPayment)
                .isInstanceOf(DomainException.class)
                .hasMessage("Only suspended policies can be canceled due to non-payment");
    }

    @Test
    @DisplayName("should be idempotent when canceling already canceled policy")
    void shouldBeIdempotentWhenCancelingAlreadyCanceledPolicy() {
        Policy policy = createValidPolicy(20, PolicyStatus.CANCELED);

        policy.cancel();
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
                status,
                status == PolicyStatus.SUSPENDED ? LocalDateTime.of(2026, 6, 11, 0, 0) : null
        );
    }
}
