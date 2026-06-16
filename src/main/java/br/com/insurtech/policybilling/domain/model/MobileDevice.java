package br.com.insurtech.policybilling.domain.model;

import java.math.BigDecimal;

import br.com.insurtech.policybilling.domain.exception.DomainException;

public record MobileDevice(
        String brand,
        String model,
        String imei,
        BigDecimal invoiceValue
) {

    public MobileDevice {
        brand = normalizeRequiredText(brand, "brand");
        model = normalizeRequiredText(model, "model");
        imei = normalizeRequiredText(imei, "imei");
        invoiceValue = validateInvoiceValue(invoiceValue);

        if (!imei.matches("\\d{15}")) {
            throw new DomainException("imei must contain exactly 15 digits");
        }
    }

    private static String normalizeRequiredText(String value, String name) {
        if (value == null) {
            throw new DomainException(name + " must not be null");
        }
        String normalized = value.trim();
        if (normalized.isBlank()) {
            throw new DomainException(name + " must not be blank");
        }
        return normalized;
    }

    private static BigDecimal validateInvoiceValue(BigDecimal invoiceValue) {
        if (invoiceValue == null) {
            throw new DomainException("invoiceValue must not be null");
        }
        if (invoiceValue.signum() <= 0) {
            throw new DomainException("invoiceValue must be positive");
        }
        return invoiceValue;
    }
}
