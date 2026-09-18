package br.com.insurtech.policybilling.infrastructure.adapter.in.web.controller;

import br.com.insurtech.policybilling.TestFixtures;
import br.com.insurtech.policybilling.application.exception.PolicyNotFoundException;
import br.com.insurtech.policybilling.application.port.in.ConfirmPaymentCommand;
import br.com.insurtech.policybilling.application.port.in.ConfirmPaymentUseCase;
import br.com.insurtech.policybilling.application.port.in.SuccessfulPaymentStatus;
import br.com.insurtech.policybilling.domain.exception.DomainException;
import br.com.insurtech.policybilling.domain.model.Policy;
import br.com.insurtech.policybilling.infrastructure.adapter.in.web.dto.PaymentWebhookRequest;
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
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentWebhookController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class PaymentWebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ConfirmPaymentUseCase confirmPaymentUseCase;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    @DisplayName("should confirm successful payment and return active policy")
    void shouldConfirmSuccessfulPaymentAndReturnActivePolicy() throws Exception {
        Policy activePolicy = TestFixtures.activePolicy();
        PaymentWebhookRequest request = new PaymentWebhookRequest(
                activePolicy.id(),
                SuccessfulPaymentStatus.SUCCEEDED
        );
        when(confirmPaymentUseCase.execute(any(ConfirmPaymentCommand.class))).thenReturn(activePolicy);

        mockMvc.perform(post("/api/v1/webhooks/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.policyId").value(activePolicy.id().toString()))
                .andExpect(jsonPath("$.policyStatus").value("ACTIVE"));

        ArgumentCaptor<ConfirmPaymentCommand> commandCaptor = ArgumentCaptor.forClass(ConfirmPaymentCommand.class);
        verify(confirmPaymentUseCase).execute(commandCaptor.capture());
        assertThat(commandCaptor.getValue().policyId()).isEqualTo(request.policyId());
        assertThat(commandCaptor.getValue().status()).isEqualTo(request.status());
    }

    @Test
    @DisplayName("should reject webhook without required fields")
    void shouldRejectWebhookWithoutRequiredFields() throws Exception {
        mockMvc.perform(post("/api/v1/webhooks/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Validation failed"))
                .andExpect(jsonPath("$.invalid_params").isArray());

        verifyNoInteractions(confirmPaymentUseCase);
    }

    @Test
    @DisplayName("should reject unsupported payment status")
    void shouldRejectUnsupportedPaymentStatus() throws Exception {
        String payload = """
                {
                  "policyId": "%s",
                  "status": "FAILED"
                }
                """.formatted(UUID.randomUUID());

        mockMvc.perform(post("/api/v1/webhooks/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(confirmPaymentUseCase);
    }

    @Test
    @DisplayName("should return 404 when policy does not exist")
    void shouldReturn404WhenPolicyDoesNotExist() throws Exception {
        UUID policyId = UUID.randomUUID();
        PaymentWebhookRequest request = new PaymentWebhookRequest(policyId, SuccessfulPaymentStatus.PAID);
        when(confirmPaymentUseCase.execute(any(ConfirmPaymentCommand.class)))
                .thenThrow(new PolicyNotFoundException(policyId));

        mockMvc.perform(post("/api/v1/webhooks/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Policy not found: " + policyId));
    }

    @Test
    @DisplayName("should return 422 when canceled policy cannot be reactivated")
    void shouldReturn422WhenCanceledPolicyCannotBeReactivated() throws Exception {
        PaymentWebhookRequest request = new PaymentWebhookRequest(
                TestFixtures.POLICY_ID,
                SuccessfulPaymentStatus.SUCCEEDED
        );
        when(confirmPaymentUseCase.execute(any(ConfirmPaymentCommand.class)))
                .thenThrow(new DomainException("Canceled policies cannot be activated after payment confirmation"));

        mockMvc.perform(post("/api/v1/webhooks/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail")
                        .value("Canceled policies cannot be activated after payment confirmation"));
    }
}
