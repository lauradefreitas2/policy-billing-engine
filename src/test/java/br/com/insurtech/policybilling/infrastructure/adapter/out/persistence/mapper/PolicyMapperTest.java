package br.com.insurtech.policybilling.infrastructure.adapter.out.persistence.mapper;

import br.com.insurtech.policybilling.TestFixtures;
import br.com.insurtech.policybilling.domain.model.CoverageType;
import br.com.insurtech.policybilling.domain.model.Policy;
import br.com.insurtech.policybilling.domain.model.PolicyStatus;
import br.com.insurtech.policybilling.infrastructure.adapter.out.persistence.entity.PolicyEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PolicyMapperTest {

    @Test
    @DisplayName("should map domain policy to persistence entity without losing business fields")
    void shouldMapDomainPolicyToPersistenceEntityWithoutLosingBusinessFields() {
        Policy policy = TestFixtures.policyWithStatus(
                TestFixtures.POLICY_ID,
                TestFixtures.CUSTOMER_ID,
                TestFixtures.DUE_DAY,
                PolicyStatus.PENDING_PAYMENT
        );

        PolicyEntity entity = PolicyMapper.toEntity(policy);

        assertThat(entity.getId()).isEqualTo(policy.id());
        assertThat(entity.getCustomerId()).isEqualTo(policy.customerId());
        assertThat(entity.getDeviceBrand()).isEqualTo(policy.device().brand());
        assertThat(entity.getDeviceModel()).isEqualTo(policy.device().model());
        assertThat(entity.getDeviceImei()).isEqualTo(policy.device().imei());
        assertThat(entity.getDeviceInvoiceValue()).isEqualByComparingTo(policy.device().invoiceValue());
        assertThat(entity.getCoverage()).isEqualTo(policy.coverage().name());
        assertThat(entity.getMonthlyPremium()).isEqualByComparingTo(policy.monthlyPremium());
        assertThat(entity.getDueDay()).isEqualTo(policy.dueDay());
        assertThat(entity.getStatus()).isEqualTo(policy.status().name());
        assertThat(entity.getSuspendedAt()).isNull();
    }

    @Test
    @DisplayName("should map persistence entity to domain policy and restore value objects")
    void shouldMapPersistenceEntityToDomainPolicyAndRestoreValueObjects() {
        PolicyEntity entity = TestFixtures.policyEntity(
                UUID.randomUUID(),
                UUID.randomUUID(),
                28,
                PolicyStatus.CANCELED
        );

        Policy policy = PolicyMapper.toDomain(entity);

        assertThat(policy.id()).isEqualTo(entity.getId());
        assertThat(policy.customerId()).isEqualTo(entity.getCustomerId());
        assertThat(policy.device().brand()).isEqualTo(entity.getDeviceBrand());
        assertThat(policy.device().model()).isEqualTo(entity.getDeviceModel());
        assertThat(policy.device().imei()).isEqualTo(entity.getDeviceImei());
        assertThat(policy.device().invoiceValue()).isEqualByComparingTo(entity.getDeviceInvoiceValue());
        assertThat(policy.coverage()).isEqualTo(CoverageType.NEW_DEVICE_REPLACEMENT);
        assertThat(policy.monthlyPremium()).isEqualByComparingTo(entity.getMonthlyPremium());
        assertThat(policy.dueDay()).isEqualTo(entity.getDueDay());
        assertThat(policy.status()).isEqualTo(PolicyStatus.CANCELED);
        assertThat(policy.suspendedAt()).isNull();
    }

    @Test
    @DisplayName("should preserve suspension timestamp in both mapping directions")
    void shouldPreserveSuspensionTimestampInBothMappingDirections() {
        Policy suspendedPolicy = TestFixtures.policyWithStatus(
                UUID.randomUUID(),
                UUID.randomUUID(),
                10,
                PolicyStatus.SUSPENDED
        );

        PolicyEntity entity = PolicyMapper.toEntity(suspendedPolicy);
        Policy restoredPolicy = PolicyMapper.toDomain(entity);

        assertThat(entity.getSuspendedAt()).isEqualTo(TestFixtures.SUSPENDED_AT);
        assertThat(restoredPolicy.status()).isEqualTo(PolicyStatus.SUSPENDED);
        assertThat(restoredPolicy.suspendedAt()).isEqualTo(TestFixtures.SUSPENDED_AT);
    }

    @Test
    @DisplayName("should reject null values explicitly")
    void shouldRejectNullValuesExplicitly() {
        assertThatThrownBy(() -> PolicyMapper.toEntity(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("policy must not be null");

        assertThatThrownBy(() -> PolicyMapper.toDomain(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("policy entity must not be null");
    }
}
