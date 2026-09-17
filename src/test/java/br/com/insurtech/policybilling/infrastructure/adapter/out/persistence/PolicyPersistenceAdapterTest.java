package br.com.insurtech.policybilling.infrastructure.adapter.out.persistence;

import br.com.insurtech.policybilling.TestFixtures;
import br.com.insurtech.policybilling.domain.model.CoverageType;
import br.com.insurtech.policybilling.domain.model.Policy;
import br.com.insurtech.policybilling.domain.model.PolicyStatus;
import br.com.insurtech.policybilling.infrastructure.adapter.out.persistence.entity.PolicyEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(PolicyPersistenceAdapter.class)
class PolicyPersistenceAdapterTest {

    @Autowired
    private PolicyPersistenceAdapter adapter;

    @Autowired
    private SpringDataPolicyRepository repository;

    @Test
    @DisplayName("should save policy successfully")
    void shouldSavePolicySuccessfully() {
        UUID id = TestFixtures.POLICY_ID;
        UUID customerId = TestFixtures.CUSTOMER_ID;
        Policy policy = TestFixtures.activePolicy(id, customerId, TestFixtures.DUE_DAY);

        Policy savedPolicy = adapter.save(policy);

        Optional<PolicyEntity> entity = repository.findById(id);
        assertThat(savedPolicy.id()).isEqualTo(id);
        assertThat(savedPolicy.customerId()).isEqualTo(customerId);
        assertThat(savedPolicy.coverage()).isEqualTo(CoverageType.NEW_DEVICE_REPLACEMENT);
        assertThat(savedPolicy.status()).isEqualTo(PolicyStatus.ACTIVE);
        assertThat(savedPolicy.monthlyPremium()).isEqualByComparingTo("99.90");
        assertThat(savedPolicy.device().invoiceValue()).isEqualByComparingTo("5999.90");

        assertThat(entity).isPresent();
        PolicyEntity persistedEntity = entity.orElseThrow();
        assertThat(persistedEntity.getCustomerId()).isEqualTo(customerId);
        assertThat(persistedEntity.getDeviceBrand()).isEqualTo(TestFixtures.DEVICE_BRAND);
        assertThat(persistedEntity.getDeviceModel()).isEqualTo(TestFixtures.DEVICE_MODEL);
        assertThat(persistedEntity.getDeviceImei()).isEqualTo(TestFixtures.DEVICE_IMEI);
        assertThat(persistedEntity.getDeviceInvoiceValue()).isEqualByComparingTo("5999.90");
        assertThat(persistedEntity.getCoverage()).isEqualTo("NEW_DEVICE_REPLACEMENT");
        assertThat(persistedEntity.getMonthlyPremium()).isEqualByComparingTo("99.90");
        assertThat(persistedEntity.getDueDay()).isEqualTo(TestFixtures.DUE_DAY);
        assertThat(persistedEntity.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("should find policy by id")
    void shouldFindPolicyById() {
        UUID id = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        PolicyEntity entity = TestFixtures.policyEntity(id, customerId, 15, PolicyStatus.PENDING_PAYMENT);
        repository.saveAndFlush(entity);

        Optional<Policy> result = adapter.findById(id);

        assertThat(result).isPresent();
        Policy policy = result.orElseThrow();
        assertThat(policy.id()).isEqualTo(id);
        assertThat(policy.customerId()).isEqualTo(customerId);
        assertThat(policy.device().brand()).isEqualTo(TestFixtures.DEVICE_BRAND);
        assertThat(policy.device().model()).isEqualTo(TestFixtures.DEVICE_MODEL);
        assertThat(policy.device().imei()).isEqualTo(TestFixtures.DEVICE_IMEI);
        assertThat(policy.device().invoiceValue()).isEqualByComparingTo("5999.90");
        assertThat(policy.coverage()).isEqualTo(CoverageType.NEW_DEVICE_REPLACEMENT);
        assertThat(policy.monthlyPremium()).isEqualByComparingTo("99.90");
        assertThat(policy.dueDay()).isEqualTo(15);
        assertThat(policy.status()).isEqualTo(PolicyStatus.PENDING_PAYMENT);
    }

    @Test
    @DisplayName("should return empty when policy is not found")
    void shouldReturnEmptyWhenPolicyNotFound() {
        Optional<Policy> result = adapter.findById(UUID.randomUUID());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("should find policies by due day and status")
    void shouldFindPoliciesByDueDayAndStatus() {
        PolicyEntity activeDuePolicy = TestFixtures.policyEntity(UUID.randomUUID(), UUID.randomUUID(), 10, PolicyStatus.ACTIVE);
        PolicyEntity pendingDuePolicy = TestFixtures.policyEntity(UUID.randomUUID(), UUID.randomUUID(), 10, PolicyStatus.PENDING_PAYMENT);
        PolicyEntity activeOtherDueDayPolicy = TestFixtures.policyEntity(UUID.randomUUID(), UUID.randomUUID(), 11, PolicyStatus.ACTIVE);
        repository.saveAllAndFlush(List.of(activeDuePolicy, pendingDuePolicy, activeOtherDueDayPolicy));

        List<Policy> result = adapter.findByDueDayAndStatus(10, PolicyStatus.ACTIVE);

        assertThat(result).hasSize(1);
        Policy policy = result.getFirst();
        assertThat(policy.id()).isEqualTo(activeDuePolicy.getId());
        assertThat(policy.dueDay()).isEqualTo(10);
        assertThat(policy.status()).isEqualTo(PolicyStatus.ACTIVE);
        assertThat(policy.coverage()).isEqualTo(CoverageType.NEW_DEVICE_REPLACEMENT);
        assertThat(policy.device().imei()).isEqualTo(TestFixtures.DEVICE_IMEI);
    }

    @Test
    @DisplayName("should find policies by status")
    void shouldFindPoliciesByStatus() {
        PolicyEntity pendingPolicy = TestFixtures.policyEntity(UUID.randomUUID(), UUID.randomUUID(), 10, PolicyStatus.PENDING_PAYMENT);
        PolicyEntity activePolicy = TestFixtures.policyEntity(UUID.randomUUID(), UUID.randomUUID(), 10, PolicyStatus.ACTIVE);
        repository.saveAllAndFlush(List.of(pendingPolicy, activePolicy));

        List<Policy> result = adapter.findByStatus(PolicyStatus.PENDING_PAYMENT);

        assertThat(result).hasSize(1);
        Policy policy = result.getFirst();
        assertThat(policy.id()).isEqualTo(pendingPolicy.getId());
        assertThat(policy.status()).isEqualTo(PolicyStatus.PENDING_PAYMENT);
        assertThat(policy.coverage()).isEqualTo(CoverageType.NEW_DEVICE_REPLACEMENT);
    }

    @Test
    @DisplayName("should update persisted policy status when saving same policy id")
    void shouldUpdatePersistedPolicyStatusWhenSavingSamePolicyId() {
        Policy policy = TestFixtures.activePolicy(UUID.randomUUID(), UUID.randomUUID(), 10);
        adapter.save(policy);

        policy.markAsPendingPayment();
        Policy updatedPolicy = adapter.save(policy);

        assertThat(updatedPolicy.status()).isEqualTo(PolicyStatus.PENDING_PAYMENT);
        assertThat(repository.findById(policy.id()))
                .isPresent()
                .get()
                .extracting(PolicyEntity::getStatus)
                .isEqualTo("PENDING_PAYMENT");
    }

    @Test
    @DisplayName("should enforce Flyway due day check constraint")
    void shouldEnforceFlywayDueDayCheckConstraint() {
        PolicyEntity invalidEntity = new PolicyEntity(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Apple",
                "iPhone 15",
                "123456789012345",
                new BigDecimal("5999.90"),
                "NEW_DEVICE_REPLACEMENT",
                new BigDecimal("99.90"),
                29,
                "ACTIVE"
        );

        assertThat(org.assertj.core.api.Assertions.catchThrowable(() -> repository.saveAndFlush(invalidEntity)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
