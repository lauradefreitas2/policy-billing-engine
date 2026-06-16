package br.com.insurtech.policybilling.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

import br.com.insurtech.policybilling.domain.exception.DomainException;

public final class Policy {

    private final UUID id;
    private final UUID customerId;
    private final MobileDevice device;
    private final CoverageType coverage;
    private final BigDecimal monthlyPremium;
    private final int dueDay;
    private PolicyStatus status;

    public static Policy issue(
            UUID id,
            UUID customerId,
            MobileDevice device,
            BigDecimal monthlyPremium,
            int dueDay
    ) {
        return new Policy(
                id,
                customerId,
                device,
                CoverageType.NEW_DEVICE_REPLACEMENT,
                monthlyPremium,
                dueDay,
                PolicyStatus.ACTIVE
        );
    }

    public Policy(
            UUID id,
            UUID customerId,
            MobileDevice device,
            CoverageType coverage,
            BigDecimal monthlyPremium,
            int dueDay,
            PolicyStatus status
    ) {
        this.id = requireNonNull(id, "id");
        this.customerId = requireNonNull(customerId, "customerId");
        this.device = requireNonNull(device, "device");
        this.coverage = requireNonNull(coverage, "coverage");
        this.monthlyPremium = validateMonthlyPremium(monthlyPremium);
        this.dueDay = validateDueDay(dueDay);
        this.status = requireNonNull(status, "status");
    }

    public void markAsPendingPayment() {
        if (status == PolicyStatus.CANCELED) {
            throw new DomainException("Canceled policies cannot be marked as pending payment");
        }
        this.status = PolicyStatus.PENDING_PAYMENT;
    }

    public void confirmPayment() {
        if (status == PolicyStatus.CANCELED) {
            throw new DomainException("Canceled policies cannot be activated after payment confirmation");
        }
        this.status = PolicyStatus.ACTIVE;
    }

    public void cancel() {
        if (status == PolicyStatus.CANCELED) {
            return;
        }
        this.status = PolicyStatus.CANCELED;
    }

    private static <T> T requireNonNull(T value, String name) {
        if (value == null) {
            throw new DomainException(name + " must not be null");
        }
        return value;
    }

    private static BigDecimal validateMonthlyPremium(BigDecimal monthlyPremium) {
        requireNonNull(monthlyPremium, "monthlyPremium");
        if (monthlyPremium.signum() <= 0) {
            throw new DomainException("monthlyPremium must be greater than zero");
        }
        return monthlyPremium;
    }

    private static int validateDueDay(int dueDay) {
        if (dueDay < 1 || dueDay > 28) {
            throw new DomainException("dueDay must be between 1 and 28");
        }
        return dueDay;
    }

    public UUID id() {
        return id;
    }

    public UUID customerId() {
        return customerId;
    }

    public MobileDevice device() {
        return device;
    }

    public CoverageType coverage() {
        return coverage;
    }

    public BigDecimal monthlyPremium() {
        return monthlyPremium;
    }

    public int dueDay() {
        return dueDay;
    }

    public PolicyStatus status() {
        return status;
    }

    public boolean isDueForBilling(LocalDate currentDate) {
        Objects.requireNonNull(currentDate, "currentDate must not be null");
        return status == PolicyStatus.ACTIVE && currentDate.getDayOfMonth() == dueDay;
    }
}
