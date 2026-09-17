package br.com.insurtech.policybilling.integration;

import br.com.insurtech.policybilling.TestFixtures;
import br.com.insurtech.policybilling.application.port.in.CancelOverduePoliciesUseCase;
import br.com.insurtech.policybilling.application.port.in.ProcessDailyBillingUseCase;
import br.com.insurtech.policybilling.application.port.in.SuspendOverduePoliciesUseCase;
import br.com.insurtech.policybilling.application.port.out.PolicyCanceledEvent;
import br.com.insurtech.policybilling.application.port.out.PolicyEventPublisherPort;
import br.com.insurtech.policybilling.domain.model.PolicyStatus;
import br.com.insurtech.policybilling.infrastructure.adapter.in.web.dto.CreatePolicyRequest;
import br.com.insurtech.policybilling.infrastructure.adapter.out.persistence.SpringDataPolicyRepository;
import br.com.insurtech.policybilling.infrastructure.adapter.out.persistence.entity.PolicyEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.quartz.auto-startup=false")
@AutoConfigureMockMvc
class PolicyBillingFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SpringDataPolicyRepository policyRepository;

    @Autowired
    private ProcessDailyBillingUseCase processDailyBillingUseCase;

    @Autowired
    private CancelOverduePoliciesUseCase cancelOverduePoliciesUseCase;

    @Autowired
    private SuspendOverduePoliciesUseCase suspendOverduePoliciesUseCase;

    @Autowired
    private MeterRegistry meterRegistry;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private PolicyEventPublisherPort policyEventPublisherPort;

    @BeforeEach
    void cleanDatabase() {
        policyRepository.deleteAll();
    }

    @Test
    @DisplayName("should create policy through API and persist it using Flyway managed schema")
    void shouldCreatePolicyThroughApiAndPersistItUsingFlywayManagedSchema() throws Exception {
        CreatePolicyRequest request = TestFixtures.validCreatePolicyRequest();
        double createdPoliciesBefore = meterRegistry.counter("policies.created").count();

        mockMvc.perform(post("/api/v1/policies")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.customerId").value(TestFixtures.CUSTOMER_ID.toString()))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        assertThat(policyRepository.findAll())
                .singleElement()
                .satisfies(entity -> {
                    assertThat(entity.getCustomerId()).isEqualTo(TestFixtures.CUSTOMER_ID);
                    assertThat(entity.getDeviceBrand()).isEqualTo(TestFixtures.DEVICE_BRAND);
                    assertThat(entity.getDeviceModel()).isEqualTo(TestFixtures.DEVICE_MODEL);
                    assertThat(entity.getDeviceImei()).isEqualTo(TestFixtures.DEVICE_IMEI);
                    assertThat(entity.getDeviceInvoiceValue()).isEqualByComparingTo(TestFixtures.DEVICE_INVOICE_VALUE);
                    assertThat(entity.getMonthlyPremium()).isEqualByComparingTo(TestFixtures.MONTHLY_PREMIUM);
                    assertThat(entity.getDueDay()).isEqualTo(TestFixtures.DUE_DAY);
                    assertThat(entity.getCoverage()).isEqualTo("NEW_DEVICE_REPLACEMENT");
                    assertThat(entity.getStatus()).isEqualTo("ACTIVE");
                });
        assertThat(meterRegistry.counter("policies.created").count()).isEqualTo(createdPoliciesBefore + 1.0);
        verifyNoInteractions(policyEventPublisherPort);
    }

    @Test
    @DisplayName("should process billing then suspend and cancel overdue policy publishing cancellation event")
    void shouldProcessBillingThenSuspendAndCancelOverduePolicyPublishingCancellationEvent() throws Exception {
        mockMvc.perform(post("/api/v1/policies")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(TestFixtures.validCreatePolicyRequest())))
                .andExpect(status().isCreated());
        PolicyEntity createdPolicy = policyRepository.findAll().getFirst();

        processDailyBillingUseCase.execute(LocalDate.of(2026, 6, TestFixtures.DUE_DAY));
        PolicyEntity pendingPolicy = policyRepository.findById(createdPolicy.getId()).orElseThrow();
        assertThat(pendingPolicy.getStatus()).isEqualTo(PolicyStatus.PENDING_PAYMENT.name());

        suspendOverduePoliciesUseCase.execute(LocalDate.of(2026, 6, TestFixtures.DUE_DAY + 1));
        PolicyEntity suspendedPolicy = policyRepository.findById(createdPolicy.getId()).orElseThrow();
        assertThat(suspendedPolicy.getStatus()).isEqualTo(PolicyStatus.SUSPENDED.name());
        assertThat(suspendedPolicy.getSuspendedAt()).isEqualTo(LocalDate.of(2026, 6, TestFixtures.DUE_DAY + 1).atStartOfDay());

        cancelOverduePoliciesUseCase.execute(LocalDate.of(2026, 6, TestFixtures.DUE_DAY + 11));

        PolicyEntity canceledPolicy = policyRepository.findById(createdPolicy.getId()).orElseThrow();
        assertThat(canceledPolicy.getStatus()).isEqualTo(PolicyStatus.CANCELED.name());
        assertThat(canceledPolicy.getSuspendedAt()).isEqualTo(LocalDate.of(2026, 6, TestFixtures.DUE_DAY + 1).atStartOfDay());

        ArgumentCaptor<PolicyCanceledEvent> eventCaptor = ArgumentCaptor.forClass(PolicyCanceledEvent.class);
        verify(policyEventPublisherPort).publishPolicyCanceledEvent(eventCaptor.capture());
        PolicyCanceledEvent event = eventCaptor.getValue();
        assertThat(event.policyId()).isEqualTo(createdPolicy.getId());
        assertThat(event.customerId()).isEqualTo(TestFixtures.CUSTOMER_ID);
        assertThat(event.canceledAt()).isNotNull();
    }
}
