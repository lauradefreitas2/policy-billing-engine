package br.com.insurtech.policybilling.domain.model;

import br.com.insurtech.policybilling.domain.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class MobileDeviceTest {

    @Test
    @DisplayName("should create mobile device when all fields are valid")
    void shouldCreateMobileDeviceWhenAllFieldsAreValid() {
        MobileDevice device = new MobileDevice(
                "Manufacturer",
                "Model X",
                "356789012345678",
                new BigDecimal("1.00")
        );

        assertThat(device).isNotNull();
        assertThat(device.brand()).isEqualTo("Manufacturer");
        assertThat(device.model()).isEqualTo("Model X");
        assertThat(device.imei()).isEqualTo("356789012345678");
        assertThat(device.invoiceValue()).isEqualByComparingTo("1.00");
    }

    @Test
    @DisplayName("should trim textual fields when creating mobile device")
    void shouldTrimTextualFieldsWhenCreatingMobileDevice() {
        MobileDevice device = new MobileDevice(
                " Manufacturer ",
                " Model X ",
                " 356789012345678 ",
                new BigDecimal("1.00")
        );

        assertThat(device.brand()).isEqualTo("Manufacturer");
        assertThat(device.model()).isEqualTo("Model X");
        assertThat(device.imei()).isEqualTo("356789012345678");
    }

    @Test
    @DisplayName("should throw DomainException when brand is null")
    void shouldThrowExceptionWhenBrandIsNull() {
        assertThatThrownBy(() -> new MobileDevice(
                null,
                "Model X",
                "356789012345678",
                new BigDecimal("1.00")
        ))
                .isInstanceOf(DomainException.class)
                .hasMessage("brand must not be null");
    }

    @Test
    @DisplayName("should throw DomainException when brand is blank")
    void shouldThrowExceptionWhenBrandIsBlank() {
        assertThatThrownBy(() -> new MobileDevice(
                " ",
                "Model X",
                "356789012345678",
                new BigDecimal("1.00")
        ))
                .isInstanceOf(DomainException.class)
                .hasMessage("brand must not be blank");
    }

    @Test
    @DisplayName("should throw DomainException when model is null")
    void shouldThrowExceptionWhenModelIsNull() {
        assertThatThrownBy(() -> new MobileDevice(
                "Manufacturer",
                null,
                "356789012345678",
                new BigDecimal("1.00")
        ))
                .isInstanceOf(DomainException.class)
                .hasMessage("model must not be null");
    }

    @Test
    @DisplayName("should throw DomainException when model is blank")
    void shouldThrowExceptionWhenModelIsBlank() {
        assertThatThrownBy(() -> new MobileDevice(
                "Manufacturer",
                " ",
                "356789012345678",
                new BigDecimal("1.00")
        ))
                .isInstanceOf(DomainException.class)
                .hasMessage("model must not be blank");
    }

    @Test
    @DisplayName("should throw DomainException when imei is null")
    void shouldThrowExceptionWhenImeiIsNull() {
        assertThatThrownBy(() -> new MobileDevice(
                "Manufacturer",
                "Model X",
                null,
                new BigDecimal("1.00")
        ))
                .isInstanceOf(DomainException.class)
                .hasMessage("imei must not be null");
    }

    @Test
    @DisplayName("should throw DomainException when imei is blank")
    void shouldThrowExceptionWhenImeiIsBlank() {
        assertThatThrownBy(() -> new MobileDevice(
                "Manufacturer",
                "Model X",
                " ",
                new BigDecimal("1.00")
        ))
                .isInstanceOf(DomainException.class)
                .hasMessage("imei must not be blank");
    }

    @Test
    @DisplayName("should throw DomainException when imei has less than 15 digits")
    void shouldThrowExceptionWhenImeiHasLessThanFifteenDigits() {
        assertThatThrownBy(() -> new MobileDevice(
                "Manufacturer",
                "Model X",
                "35678901234567",
                new BigDecimal("1.00")
        ))
                .isInstanceOf(DomainException.class)
                .hasMessage("imei must contain exactly 15 digits");
    }

    @Test
    @DisplayName("should throw DomainException when imei contains non digit characters")
    void shouldThrowExceptionWhenImeiContainsNonDigitCharacters() {
        assertThatThrownBy(() -> new MobileDevice(
                "Manufacturer",
                "Model X",
                "35678901234567A",
                new BigDecimal("1.00")
        ))
                .isInstanceOf(DomainException.class)
                .hasMessage("imei must contain exactly 15 digits");
    }

    @Test
    @DisplayName("should throw DomainException when invoice value is null")
    void shouldThrowExceptionWhenInvoiceValueIsNull() {
        assertThatThrownBy(() -> new MobileDevice(
                "Manufacturer",
                "Model X",
                "356789012345678",
                null
        ))
                .isInstanceOf(DomainException.class)
                .hasMessage("invoiceValue must not be null");
    }

    @Test
    @DisplayName("should throw DomainException when invoice value is zero")
    void shouldThrowExceptionWhenInvoiceValueIsZero() {
        assertThatThrownBy(() -> new MobileDevice(
                "Manufacturer",
                "Model X",
                "356789012345678",
                BigDecimal.ZERO
        ))
                .isInstanceOf(DomainException.class)
                .hasMessage("invoiceValue must be positive");
    }

    @Test
    @DisplayName("should throw DomainException when invoice value is negative")
    void shouldThrowExceptionWhenInvoiceValueIsNegative() {
        assertThatThrownBy(() -> new MobileDevice(
                "Manufacturer",
                "Model X",
                "356789012345678",
                new BigDecimal("-1.00")
        ))
                .isInstanceOf(DomainException.class)
                .hasMessage("invoiceValue must be positive");
    }
}
