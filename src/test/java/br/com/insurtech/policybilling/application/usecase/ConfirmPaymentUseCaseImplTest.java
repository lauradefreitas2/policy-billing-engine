package br.com.insurtech.policybilling.application.usecase;

import br.com.insurtech.policybilling.TestFixtures;
import br.com.insurtech.policybilling.application.exception.PolicyNotFoundException;
import br.com.insurtech.policybilling.application.port.in.ConfirmPaymentCommand;
import br.com.insurtech.policybilling.application.port.in.SuccessfulPaymentStatus;
import br.com.insurtech.policybilling.application.port.out.PolicyRepositoryPort;
import br.com.insurtech.policybilling.domain.exception.DomainException;
import br.com.insurtech.policybilling.domain.model.Policy;
import br.com.insurtech.policybilling.domain.model.PolicyStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfirmPaymentUseCaseImplTest {

    @Mock
    private PolicyRepositoryPort policyRepositoryPort;

    @InjectMocks
    private ConfirmPaymentUseCaseImpl confirmPaymentUseCase;

    @Test
    @DisplayName("should reactivate suspended policy and clear suspension date")
    void shouldReactivateSuspendedPolicyAndClearSuspensionDate() {
        Policy suspendedPolicy = TestFixtures.policyWithStatus(PolicyStatus.SUSPENDED);
        ConfirmPaymentCommand command = commandFor(suspendedPolicy.id());
        when(policyRepositoryPort.findById(suspendedPolicy.id())).thenReturn(Optional.of(suspendedPolicy));
        doAnswer(invocation -> invocation.getArgument(0))
                .when(policyRepositoryPort)
                .save(any(Policy.class));

        Policy result = confirmPaymentUseCase.execute(command);

        assertThat(result.status()).isEqualTo(PolicyStatus.ACTIVE);
        assertThat(result.suspendedAt()).isNull();
        ArgumentCaptor<Policy> policyCaptor = ArgumentCaptor.forClass(Policy.class);
        verify(policyRepositoryPort).save(policyCaptor.capture());
        assertThat(policyCaptor.getValue().status()).isEqualTo(PolicyStatus.ACTIVE);
        assertThat(policyCaptor.getValue().suspendedAt()).isNull();
    }

    @Test
    @DisplayName("should activate pending payment policy")
    void shouldActivatePendingPaymentPolicy() {
        Policy pendingPolicy = TestFixtures.policyWithStatus(PolicyStatus.PENDING_PAYMENT);
        when(policyRepositoryPort.findById(pendingPolicy.id())).thenReturn(Optional.of(pendingPolicy));
        doAnswer(invocation -> invocation.getArgument(0))
                .when(policyRepositoryPort)
                .save(any(Policy.class));

        Policy result = confirmPaymentUseCase.execute(commandFor(pendingPolicy.id()));

        assertThat(result.status()).isEqualTo(PolicyStatus.ACTIVE);
        assertThat(result.suspendedAt()).isNull();
        verify(policyRepositoryPort).save(pendingPolicy);
    }

    @Test
    @DisplayName("should handle repeated successful webhook idempotently for active policy")
    void shouldHandleRepeatedWebhookIdempotentlyForActivePolicy() {
        Policy activePolicy = TestFixtures.activePolicy();
        when(policyRepositoryPort.findById(activePolicy.id())).thenReturn(Optional.of(activePolicy));
        doAnswer(invocation -> invocation.getArgument(0))
                .when(policyRepositoryPort)
                .save(any(Policy.class));

        Policy result = confirmPaymentUseCase.execute(commandFor(activePolicy.id()));

        assertThat(result.status()).isEqualTo(PolicyStatus.ACTIVE);
        assertThat(result.suspendedAt()).isNull();
        verify(policyRepositoryPort).save(activePolicy);
    }

    @Test
    @DisplayName("should reject payment confirmation for canceled policy")
    void shouldRejectPaymentConfirmationForCanceledPolicy() {
        Policy canceledPolicy = TestFixtures.policyWithStatus(PolicyStatus.CANCELED);
        when(policyRepositoryPort.findById(canceledPolicy.id())).thenReturn(Optional.of(canceledPolicy));

        assertThatThrownBy(() -> confirmPaymentUseCase.execute(commandFor(canceledPolicy.id())))
                .isInstanceOf(DomainException.class)
                .hasMessage("Canceled policies cannot be activated after payment confirmation");

        verify(policyRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("should report policy not found without trying to save")
    void shouldReportPolicyNotFoundWithoutTryingToSave() {
        UUID policyId = UUID.randomUUID();
        when(policyRepositoryPort.findById(policyId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> confirmPaymentUseCase.execute(commandFor(policyId)))
                .isInstanceOf(PolicyNotFoundException.class)
                .hasMessage("Policy not found: " + policyId);

        verify(policyRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("should reject null command")
    void shouldRejectNullCommand() {
        assertThatThrownBy(() -> confirmPaymentUseCase.execute(null))
                .isInstanceOf(DomainException.class)
                .hasMessage("confirm payment command must not be null");

        verifyNoInteractions(policyRepositoryPort);
    }

    @Test
    @DisplayName("should reject command without policy id")
    void shouldRejectCommandWithoutPolicyId() {
        ConfirmPaymentCommand command = new ConfirmPaymentCommand(null, SuccessfulPaymentStatus.PAID);

        assertThatThrownBy(() -> confirmPaymentUseCase.execute(command))
                .isInstanceOf(DomainException.class)
                .hasMessage("policyId must not be null");

        verifyNoInteractions(policyRepositoryPort);
    }

    @Test
    @DisplayName("should reject command without payment status")
    void shouldRejectCommandWithoutPaymentStatus() {
        ConfirmPaymentCommand command = new ConfirmPaymentCommand(UUID.randomUUID(), null);

        assertThatThrownBy(() -> confirmPaymentUseCase.execute(command))
                .isInstanceOf(DomainException.class)
                .hasMessage("payment status must not be null");

        verifyNoInteractions(policyRepositoryPort);
    }

    @Test
    @DisplayName("should reject null repository")
    void shouldRejectNullRepository() {
        assertThatThrownBy(() -> new ConfirmPaymentUseCaseImpl(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("policyRepositoryPort must not be null");
    }

    private static ConfirmPaymentCommand commandFor(UUID policyId) {
        return new ConfirmPaymentCommand(policyId, SuccessfulPaymentStatus.SUCCEEDED);
    }
}
