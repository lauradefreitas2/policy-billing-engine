package br.com.insurtech.policybilling.domain.model;

import br.com.insurtech.policybilling.domain.exception.DomainException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidDevices")
    @DisplayName("should reject invalid mobile device data")
    void shouldRejectInvalidMobileDeviceData(
            String scenario,
            String brand,
            String model,
            String imei,
            BigDecimal invoiceValue,
            String expectedMessage
    ) {
        assertThatThrownBy(() -> new MobileDevice(brand, model, imei, invoiceValue))
                .isInstanceOf(DomainException.class)
                .hasMessage(expectedMessage);
    }

    private static Stream<Arguments> invalidDevices() {
        return Stream.of(
                Arguments.of("brand null", null, "Model X", "356789012345678", new BigDecimal("1.00"), "brand must not be null"),
                Arguments.of("brand blank", " ", "Model X", "356789012345678", new BigDecimal("1.00"), "brand must not be blank"),
                Arguments.of("model null", "Manufacturer", null, "356789012345678", new BigDecimal("1.00"), "model must not be null"),
                Arguments.of("model blank", "Manufacturer", " ", "356789012345678", new BigDecimal("1.00"), "model must not be blank"),
                Arguments.of("imei null", "Manufacturer", "Model X", null, new BigDecimal("1.00"), "imei must not be null"),
                Arguments.of("imei blank", "Manufacturer", "Model X", " ", new BigDecimal("1.00"), "imei must not be blank"),
                Arguments.of("imei too short", "Manufacturer", "Model X", "35678901234567", new BigDecimal("1.00"), "imei must contain exactly 15 digits"),
                Arguments.of("imei too long", "Manufacturer", "Model X", "3567890123456789", new BigDecimal("1.00"), "imei must contain exactly 15 digits"),
                Arguments.of("imei non numeric", "Manufacturer", "Model X", "35678901234567A", new BigDecimal("1.00"), "imei must contain exactly 15 digits"),
                Arguments.of("invoice null", "Manufacturer", "Model X", "356789012345678", null, "invoiceValue must not be null"),
                Arguments.of("invoice zero", "Manufacturer", "Model X", "356789012345678", BigDecimal.ZERO, "invoiceValue must be positive"),
                Arguments.of("invoice negative", "Manufacturer", "Model X", "356789012345678", new BigDecimal("-1.00"), "invoiceValue must be positive")
        );
    }
}
