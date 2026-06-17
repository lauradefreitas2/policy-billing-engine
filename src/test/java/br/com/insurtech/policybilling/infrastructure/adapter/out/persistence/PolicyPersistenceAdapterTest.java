package br.com.insurtech.policybilling.infrastructure.adapter.out.persistence;

import br.com.insurtech.policybilling.domain.model.CoverageType;
import br.com.insurtech.policybilling.domain.model.MobileDevice;
import br.com.insurtech.policybilling.domain.model.Policy;
import br.com.insurtech.policybilling.domain.model.PolicyStatus;
import br.com.insurtech.policybilling.infrastructure.adapter.out.persistence.entity.PolicyEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

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
        UUID id = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        Policy policy = Policy.issue(
                id,
                customerId,
                new MobileDevice("Apple", "iPhone 15", "123456789012345", new BigDecimal("5999.90")),
                new BigDecimal("99.90"),
                10
        );

        Policy savedPolicy = adapter.save(policy);

        Optional<PolicyEntity> entity = repository.findById(id);
        assertThat(savedPolicy.id()).isEqualTo(id);
        assertThat(savedPolicy.customerId()).isEqualTo(customerId);
        assertThat(savedPolicy.coverage()).isEqualTo(CoverageType.NEW_DEVICE_REPLACEMENT);
        assertThat(savedPolicy.status()).isEqualTo(PolicyStatus.ACTIVE);

        assertThat(entity).isPresent();
        PolicyEntity persistedEntity = entity.orElseThrow();
        assertThat(persistedEntity.getCustomerId()).isEqualTo(customerId);
        assertThat(persistedEntity.getDeviceBrand()).isEqualTo("Apple");
        assertThat(persistedEntity.getDeviceModel()).isEqualTo("iPhone 15");
        assertThat(persistedEntity.getDeviceImei()).isEqualTo("123456789012345");
        assertThat(persistedEntity.getDeviceInvoiceValue()).isEqualByComparingTo("5999.90");
        assertThat(persistedEntity.getCoverage()).isEqualTo("NEW_DEVICE_REPLACEMENT");
        assertThat(persistedEntity.getMonthlyPremium()).isEqualByComparingTo("99.90");
        assertThat(persistedEntity.getDueDay()).isEqualTo(10);
        assertThat(persistedEntity.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("should find policy by id")
    void shouldFindPolicyById() {
        UUID id = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        PolicyEntity entity = new PolicyEntity(
                id,
                customerId,
                "Samsung",
                "Galaxy S26",
                "543210987654321",
                new BigDecimal("4499.90"),
                "NEW_DEVICE_REPLACEMENT",
                new BigDecimal("79.90"),
                15,
                "PENDING_PAYMENT"
        );
        repository.saveAndFlush(entity);

        Optional<Policy> result = adapter.findById(id);

        assertThat(result).isPresent();
        Policy policy = result.orElseThrow();
        assertThat(policy.id()).isEqualTo(id);
        assertThat(policy.customerId()).isEqualTo(customerId);
        assertThat(policy.device().brand()).isEqualTo("Samsung");
        assertThat(policy.device().model()).isEqualTo("Galaxy S26");
        assertThat(policy.device().imei()).isEqualTo("543210987654321");
        assertThat(policy.device().invoiceValue()).isEqualByComparingTo("4499.90");
        assertThat(policy.coverage()).isEqualTo(CoverageType.NEW_DEVICE_REPLACEMENT);
        assertThat(policy.monthlyPremium()).isEqualByComparingTo("79.90");
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
        PolicyEntity activeDuePolicy = new PolicyEntity(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Apple",
                "iPhone 15",
                "123456789012345",
                new BigDecimal("5999.90"),
                "NEW_DEVICE_REPLACEMENT",
                new BigDecimal("99.90"),
                10,
                "ACTIVE"
        );
        PolicyEntity pendingDuePolicy = new PolicyEntity(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Samsung",
                "Galaxy S26",
                "543210987654321",
                new BigDecimal("4499.90"),
                "NEW_DEVICE_REPLACEMENT",
                new BigDecimal("79.90"),
                10,
                "PENDING_PAYMENT"
        );
        PolicyEntity activeOtherDueDayPolicy = new PolicyEntity(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Motorola",
                "Edge",
                "111222333444555",
                new BigDecimal("2999.90"),
                "NEW_DEVICE_REPLACEMENT",
                new BigDecimal("59.90"),
                11,
                "ACTIVE"
        );
        repository.saveAllAndFlush(List.of(activeDuePolicy, pendingDuePolicy, activeOtherDueDayPolicy));

        List<Policy> result = adapter.findByDueDayAndStatus(10, PolicyStatus.ACTIVE);

        assertThat(result).hasSize(1);
        Policy policy = result.getFirst();
        assertThat(policy.id()).isEqualTo(activeDuePolicy.getId());
        assertThat(policy.dueDay()).isEqualTo(10);
        assertThat(policy.status()).isEqualTo(PolicyStatus.ACTIVE);
        assertThat(policy.coverage()).isEqualTo(CoverageType.NEW_DEVICE_REPLACEMENT);
        assertThat(policy.device().imei()).isEqualTo("123456789012345");
    }
}
