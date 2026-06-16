package br.com.insurtech.policybilling.infrastructure.adapter.in.web.controller;

import br.com.insurtech.policybilling.application.port.in.CreatePolicyCommand;
import br.com.insurtech.policybilling.application.port.in.CreatePolicyUseCase;
import br.com.insurtech.policybilling.domain.exception.DomainException;
import br.com.insurtech.policybilling.domain.model.CoverageType;
import br.com.insurtech.policybilling.domain.model.MobileDevice;
import br.com.insurtech.policybilling.domain.model.Policy;
import br.com.insurtech.policybilling.domain.model.PolicyStatus;
import br.com.insurtech.policybilling.infrastructure.adapter.in.web.dto.CreatePolicyRequest;
import br.com.insurtech.policybilling.infrastructure.adapter.in.web.exception.GlobalExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PolicyController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class PolicyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreatePolicyUseCase createPolicyUseCase;

    @Test
    @DisplayName("should return 201 Created when request is valid")
    void shouldReturn201CreatedWhenRequestIsValid() throws Exception {
        UUID customerId = UUID.randomUUID();
        CreatePolicyRequest request = new CreatePolicyRequest(
                customerId,
                "TestBrand",
                "TestModel",
                "123456789012345",
                new BigDecimal("399.99"),
                new BigDecimal("99.90"),
                10
        );

        Policy savedPolicy = new Policy(
                UUID.randomUUID(),
                customerId,
                new MobileDevice("TestBrand", "TestModel", "123456789012345", new BigDecimal("399.99")),
                CoverageType.NEW_DEVICE_REPLACEMENT,
                new BigDecimal("99.90"),
                10,
                PolicyStatus.ACTIVE
        );

        when(createPolicyUseCase.execute(any(CreatePolicyCommand.class))).thenReturn(savedPolicy);

        mockMvc.perform(post("/api/v1/policies")
                        .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON, "content type must not be null"))
                        .content(Objects.requireNonNull(objectMapper.writeValueAsString(request), "request body must not be null")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(savedPolicy.id().toString()))
                .andExpect(jsonPath("$.customerId").value(customerId.toString()))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        ArgumentCaptor<CreatePolicyCommand> commandCaptor = ArgumentCaptor.forClass(CreatePolicyCommand.class);
        verify(createPolicyUseCase).execute(commandCaptor.capture());

        CreatePolicyCommand command = commandCaptor.getValue();
        assertThat(command.customerId()).isEqualTo(request.customerId());
        assertThat(command.deviceBrand()).isEqualTo(request.deviceBrand());
        assertThat(command.deviceModel()).isEqualTo(request.deviceModel());
        assertThat(command.deviceImei()).isEqualTo(request.deviceImei());
        assertThat(command.deviceInvoiceValue()).isEqualByComparingTo(request.deviceInvoiceValue());
        assertThat(command.monthlyPremium()).isEqualByComparingTo(request.monthlyPremium());
        assertThat(command.dueDay()).isEqualTo(request.dueDay());
    }

    @Test
    @DisplayName("should return 400 Bad Request when validation fails")
    void shouldReturn400BadRequestWhenValidationFails() throws Exception {
        CreatePolicyRequest invalidRequest = new CreatePolicyRequest(
                UUID.randomUUID(),
                "",
                "TestModel",
                "123456789012345",
                new BigDecimal("399.99"),
                new BigDecimal("-10.00"),
                0
        );

        mockMvc.perform(post("/api/v1/policies")
                        .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON, "content type must not be null"))
                        .content(Objects.requireNonNull(objectMapper.writeValueAsString(invalidRequest), "request body must not be null")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").exists())
                .andExpect(jsonPath("$.invalid_params").isArray());

        verifyNoInteractions(createPolicyUseCase);
    }

    @Test
    @DisplayName("should return 400 Bad Request when imei format is invalid")
    void shouldReturn400BadRequestWhenImeiFormatIsInvalid() throws Exception {
        CreatePolicyRequest invalidRequest = new CreatePolicyRequest(
                UUID.randomUUID(),
                "TestBrand",
                "TestModel",
                "12345678901234A",
                new BigDecimal("399.99"),
                new BigDecimal("99.90"),
                10
        );

        mockMvc.perform(post("/api/v1/policies")
                        .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON, "content type must not be null"))
                        .content(Objects.requireNonNull(objectMapper.writeValueAsString(invalidRequest), "request body must not be null")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.invalid_params").isArray());

        verifyNoInteractions(createPolicyUseCase);
    }

    @Test
    @DisplayName("should return 422 Unprocessable Entity when domain rule is violated")
    void shouldReturn422UnprocessableEntityWhenDomainRuleIsViolated() throws Exception {
        UUID customerId = UUID.randomUUID();
        CreatePolicyRequest request = new CreatePolicyRequest(
                customerId,
                "TestBrand",
                "TestModel",
                "123456789012345",
                new BigDecimal("399.99"),
                new BigDecimal("99.90"),
                10
        );

        when(createPolicyUseCase.execute(any(CreatePolicyCommand.class)))
                .thenThrow(new DomainException("Custom domain error"));

        mockMvc.perform(post("/api/v1/policies")
                        .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON, "content type must not be null"))
                        .content(Objects.requireNonNull(objectMapper.writeValueAsString(request), "request body must not be null")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value("Custom domain error"));
    }
}
