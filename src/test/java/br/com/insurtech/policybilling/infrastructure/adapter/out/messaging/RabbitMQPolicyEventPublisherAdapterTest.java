package br.com.insurtech.policybilling.infrastructure.adapter.out.messaging;

import br.com.insurtech.policybilling.application.port.out.PolicyCanceledEvent;
import br.com.insurtech.policybilling.infrastructure.config.RabbitMQConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.mock;
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
                LocalDateTime.of(2026, 9, 17, 18, 30)
        );

        adapter.publishPolicyCanceledEvent(event);

        verify(rabbitTemplate).convertAndSend(
                RabbitMQConfig.POLICY_EVENTS_EXCHANGE,
                RabbitMQConfig.POLICY_CANCELED_ROUTING_KEY,
                event
        );
    }
}
