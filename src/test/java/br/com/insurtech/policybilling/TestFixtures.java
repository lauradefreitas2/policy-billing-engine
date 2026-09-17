package br.com.insurtech.policybilling;

import br.com.insurtech.policybilling.application.port.in.CreatePolicyCommand;
import br.com.insurtech.policybilling.domain.model.CoverageType;
import br.com.insurtech.policybilling.domain.model.MobileDevice;
import br.com.insurtech.policybilling.domain.model.Policy;
import br.com.insurtech.policybilling.domain.model.PolicyStatus;
import br.com.insurtech.policybilling.infrastructure.adapter.in.web.dto.CreatePolicyRequest;
import br.com.insurtech.policybilling.infrastructure.adapter.out.persistence.entity.PolicyEntity;

import java.math.BigDecimal;
import java.util.UUID;

public final class TestFixtures {

    public static final UUID CUSTOMER_ID = UUID.fromString("7f3a7d7b-9c52-41af-9f8d-41c6fce8f924");
    public static final UUID POLICY_ID = UUID.fromString("a3cdd876-7f2a-4bd0-a62e-a1859388657d");
    public static final String DEVICE_BRAND = "Apple";
    public static final String DEVICE_MODEL = "iPhone 15";
    public static final String DEVICE_IMEI = "123456789012345";
    public static final BigDecimal DEVICE_INVOICE_VALUE = new BigDecimal("5999.90");
    public static final BigDecimal MONTHLY_PREMIUM = new BigDecimal("99.90");
    public static final int DUE_DAY = 10;

    private TestFixtures() {
    }

    public static MobileDevice validMobileDevice() {
        return new MobileDevice(DEVICE_BRAND, DEVICE_MODEL, DEVICE_IMEI, DEVICE_INVOICE_VALUE);
    }

    public static Policy activePolicy() {
        return activePolicy(POLICY_ID, CUSTOMER_ID, DUE_DAY);
    }

    public static Policy activePolicy(UUID id, UUID customerId, int dueDay) {
        return Policy.issue(id, customerId, validMobileDevice(), MONTHLY_PREMIUM, dueDay);
    }

    public static Policy policyWithStatus(PolicyStatus status) {
        return policyWithStatus(UUID.randomUUID(), CUSTOMER_ID, DUE_DAY, status);
    }

    public static Policy policyWithStatus(UUID id, UUID customerId, int dueDay, PolicyStatus status) {
        return new Policy(
                id,
                customerId,
                validMobileDevice(),
                CoverageType.NEW_DEVICE_REPLACEMENT,
                MONTHLY_PREMIUM,
                dueDay,
                status
        );
    }

    public static CreatePolicyCommand validCreatePolicyCommand() {
        return new CreatePolicyCommand(
                CUSTOMER_ID,
                DEVICE_BRAND,
                DEVICE_MODEL,
                DEVICE_IMEI,
                DEVICE_INVOICE_VALUE,
                MONTHLY_PREMIUM,
                DUE_DAY
        );
    }

    public static CreatePolicyRequest validCreatePolicyRequest() {
        return new CreatePolicyRequest(
                CUSTOMER_ID,
                DEVICE_BRAND,
                DEVICE_MODEL,
                DEVICE_IMEI,
                DEVICE_INVOICE_VALUE,
                MONTHLY_PREMIUM,
                DUE_DAY
        );
    }

    public static PolicyEntity policyEntity(UUID id, UUID customerId, int dueDay, PolicyStatus status) {
        return new PolicyEntity(
                id,
                customerId,
                DEVICE_BRAND,
                DEVICE_MODEL,
                DEVICE_IMEI,
                DEVICE_INVOICE_VALUE,
                CoverageType.NEW_DEVICE_REPLACEMENT.name(),
                MONTHLY_PREMIUM,
                dueDay,
                status.name()
        );
    }
}
