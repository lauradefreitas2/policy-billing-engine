package br.com.insurtech.policybilling.infrastructure.adapter.out.messaging;

import br.com.insurtech.policybilling.application.port.out.PolicyCanceledEvent;
import br.com.insurtech.policybilling.infrastructure.config.RabbitMQConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.same;
import static org.mockito.Mockito.verify;

class RabbitMQPolicyEventPublisherAdapterTest {

    @Test
    @DisplayName("should publish policy canceled event to configured exchange and routing key")
    void shouldPublishPolicyCanceledEventToConfiguredExchangeAndRoutingKey() {
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        RabbitMQPolicyEventPublisherAdapter adapter = new RabbitMQPolicyEventPublisherAdapter(rabbitTemplate);
        PolicyCanceledEvent event = new PolicyCanceledEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                Instant.parse("2026-09-17T18:30:00Z")
        );
        doAnswer(invocation -> {
            CorrelationData correlationData = invocation.getArgument(4);
            correlationData.getFuture().complete(new CorrelationData.Confirm(true, null));
            return null;
        }).when(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.POLICY_EVENTS_EXCHANGE),
                eq(RabbitMQConfig.POLICY_CANCELED_ROUTING_KEY),
                same(event),
                any(MessagePostProcessor.class),
                any(CorrelationData.class)
        );

        adapter.publishPolicyCanceledEvent(event);

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.POLICY_EVENTS_EXCHANGE),
                eq(RabbitMQConfig.POLICY_CANCELED_ROUTING_KEY),
                same(event),
                any(MessagePostProcessor.class),
                any(CorrelationData.class)
        );
    }

    @Test
    @DisplayName("should fail when RabbitMQ rejects the event")
    void shouldFailWhenRabbitMqRejectsTheEvent() {
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        RabbitMQPolicyEventPublisherAdapter adapter = new RabbitMQPolicyEventPublisherAdapter(rabbitTemplate);
        PolicyCanceledEvent event = new PolicyCanceledEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                Instant.parse("2026-09-17T18:30:00Z")
        );
        doAnswer(invocation -> {
            CorrelationData correlationData = invocation.getArgument(4);
            correlationData.getFuture().complete(new CorrelationData.Confirm(false, "broker unavailable"));
            return null;
        }).when(rabbitTemplate).convertAndSend(
                any(String.class),
                any(String.class),
                same(event),
                any(MessagePostProcessor.class),
                any(CorrelationData.class)
        );

        assertThatThrownBy(() -> adapter.publishPolicyCanceledEvent(event))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("broker unavailable");
    }
}
