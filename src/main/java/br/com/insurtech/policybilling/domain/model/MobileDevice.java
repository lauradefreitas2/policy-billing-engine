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
        validateNonNull(brand, "brand");
        validateNonNull(model, "model");
        validateNonNull(imei, "imei");
        validateNonNull(invoiceValue, "invoiceValue");

        if (brand.isBlank()) {
            throw new DomainException("brand must not be blank");
        }
        if (model.isBlank()) {
            throw new DomainException("model must not be blank");
        }
        if (imei.isBlank()) {
            throw new DomainException("imei must not be blank");
        }
        if (invoiceValue.signum() <= 0) {
            throw new DomainException("invoiceValue must be positive");
        }
    }

    private static <T> T validateNonNull(T value, String name) {
        if (value == null) {
            throw new DomainException(name + " must not be null");
        }
        return value;
    }
}
