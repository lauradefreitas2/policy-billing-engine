package br.com.insurtech.policybilling.integration;

import br.com.insurtech.policybilling.TestFixtures;
import br.com.insurtech.policybilling.application.port.in.CancelOverduePoliciesUseCase;
import br.com.insurtech.policybilling.application.port.out.PolicyEventOutboxPort;
import br.com.insurtech.policybilling.domain.model.PolicyStatus;
import br.com.insurtech.policybilling.infrastructure.adapter.out.persistence.SpringDataPolicyRepository;
import br.com.insurtech.policybilling.infrastructure.adapter.out.persistence.entity.PolicyEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest(properties = "spring.quartz.auto-startup=false")
class TransactionalOutboxIntegrationTest {

    @Autowired
    private CancelOverduePoliciesUseCase cancelOverduePoliciesUseCase;

    @Autowired
    private SpringDataPolicyRepository policyRepository;

    @MockitoBean
    private PolicyEventOutboxPort policyEventOutboxPort;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    @DisplayName("should roll back policy cancellation when outbox append fails")
    void shouldRollBackPolicyCancellationWhenOutboxAppendFails() {
        PolicyEntity policy = new PolicyEntity(
                UUID.randomUUID(),
                UUID.randomUUID(),
                TestFixtures.DEVICE_BRAND,
                TestFixtures.DEVICE_MODEL,
                TestFixtures.DEVICE_IMEI,
                TestFixtures.DEVICE_INVOICE_VALUE,
                "NEW_DEVICE_REPLACEMENT",
                TestFixtures.MONTHLY_PREMIUM,
                TestFixtures.DUE_DAY,
                PolicyStatus.SUSPENDED.name(),
                LocalDateTime.of(2026, 6, 11, 0, 0)
        );
        policyRepository.saveAndFlush(policy);
        doThrow(new IllegalStateException("outbox unavailable"))
                .when(policyEventOutboxPort)
                .appendPolicyCanceledEvent(any());

        assertThatThrownBy(() -> cancelOverduePoliciesUseCase.execute(LocalDate.of(2026, 6, 21)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("outbox unavailable");

        assertThat(policyRepository.findById(policy.getId()).orElseThrow().getStatus())
                .isEqualTo(PolicyStatus.SUSPENDED.name());
    }
}
