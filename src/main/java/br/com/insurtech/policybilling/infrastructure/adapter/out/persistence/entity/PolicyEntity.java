package br.com.insurtech.policybilling.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "policies")
public class PolicyEntity {

    @Id
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "device_brand", nullable = false)
    private String deviceBrand;

    @Column(name = "device_model", nullable = false)
    private String deviceModel;

    @Column(name = "device_imei", nullable = false, length = 15)
    private String deviceImei;

    @Column(name = "device_invoice_value", nullable = false, precision = 19, scale = 2)
    private BigDecimal deviceInvoiceValue;

    @Column(name = "coverage", nullable = false)
    private String coverage;

    @Column(name = "monthly_premium", nullable = false, precision = 19, scale = 2)
    private BigDecimal monthlyPremium;

    @Column(name = "due_day", nullable = false)
    private int dueDay;

    @Column(name = "status", nullable = false)
    private String status;

    protected PolicyEntity() {
    }

    public PolicyEntity(
            UUID id,
            UUID customerId,
            String deviceBrand,
            String deviceModel,
            String deviceImei,
            BigDecimal deviceInvoiceValue,
            String coverage,
            BigDecimal monthlyPremium,
            int dueDay,
            String status
    ) {
        this.id = id;
        this.customerId = customerId;
        this.deviceBrand = deviceBrand;
        this.deviceModel = deviceModel;
        this.deviceImei = deviceImei;
        this.deviceInvoiceValue = deviceInvoiceValue;
        this.coverage = coverage;
        this.monthlyPremium = monthlyPremium;
        this.dueDay = dueDay;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public String getDeviceBrand() {
        return deviceBrand;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public String getDeviceImei() {
        return deviceImei;
    }

    public BigDecimal getDeviceInvoiceValue() {
        return deviceInvoiceValue;
    }

    public String getCoverage() {
        return coverage;
    }

    public BigDecimal getMonthlyPremium() {
        return monthlyPremium;
    }

    public int getDueDay() {
        return dueDay;
    }

    public String getStatus() {
        return status;
    }
}
