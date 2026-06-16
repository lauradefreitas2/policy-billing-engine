package br.com.insurtech.policybilling.infrastructure.adapter.out.persistence.mapper;

import br.com.insurtech.policybilling.domain.model.CoverageType;
import br.com.insurtech.policybilling.domain.model.MobileDevice;
import br.com.insurtech.policybilling.domain.model.Policy;
import br.com.insurtech.policybilling.domain.model.PolicyStatus;
import br.com.insurtech.policybilling.infrastructure.adapter.out.persistence.entity.PolicyEntity;

import java.util.Objects;

public final class PolicyMapper {

    private PolicyMapper() {
    }

    public static PolicyEntity toEntity(Policy policy) {
        Objects.requireNonNull(policy, "policy must not be null");

        return new PolicyEntity(
                policy.id(),
                policy.customerId(),
                policy.device().brand(),
                policy.device().model(),
                policy.device().imei(),
                policy.device().invoiceValue(),
                policy.coverage().name(),
                policy.monthlyPremium(),
                policy.dueDay(),
                policy.status().name()
        );
    }

    public static Policy toDomain(PolicyEntity entity) {
        Objects.requireNonNull(entity, "policy entity must not be null");

        MobileDevice device = new MobileDevice(
                entity.getDeviceBrand(),
                entity.getDeviceModel(),
                entity.getDeviceImei(),
                entity.getDeviceInvoiceValue()
        );

        return new Policy(
                entity.getId(),
                entity.getCustomerId(),
                device,
                CoverageType.valueOf(entity.getCoverage()),
                entity.getMonthlyPremium(),
                entity.getDueDay(),
                PolicyStatus.valueOf(entity.getStatus())
        );
    }
}
