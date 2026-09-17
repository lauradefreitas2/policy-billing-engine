package br.com.insurtech.policybilling.infrastructure.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;

import static org.assertj.core.api.Assertions.assertThat;

class RabbitMQConfigTest {

    private final RabbitMQConfig config = new RabbitMQConfig();

    @Test
    @DisplayName("should expose durable policy events exchange and cancellation queue")
    void shouldExposeDurablePolicyEventsExchangeAndCancellationQueue() {
        DirectExchange exchange = config.policyEventsExchange();
        Queue queue = config.policyCanceledQueue();

        assertThat(exchange.getName()).isEqualTo(RabbitMQConfig.POLICY_EVENTS_EXCHANGE);
        assertThat(exchange.isDurable()).isTrue();
        assertThat(queue.getName()).isEqualTo(RabbitMQConfig.POLICY_CANCELED_QUEUE);
        assertThat(queue.isDurable()).isTrue();
    }

    @Test
    @DisplayName("should bind cancellation queue to policy events exchange with routing key")
    void shouldBindCancellationQueueToPolicyEventsExchangeWithRoutingKey() {
        DirectExchange exchange = config.policyEventsExchange();
        Queue queue = config.policyCanceledQueue();

        Binding binding = config.policyCanceledBinding(queue, exchange);

        assertThat(binding.getDestination()).isEqualTo(RabbitMQConfig.POLICY_CANCELED_QUEUE);
        assertThat(binding.getExchange()).isEqualTo(RabbitMQConfig.POLICY_EVENTS_EXCHANGE);
        assertThat(binding.getRoutingKey()).isEqualTo(RabbitMQConfig.POLICY_CANCELED_ROUTING_KEY);
    }

    @Test
    @DisplayName("should use Jackson JSON converter for readable event payloads")
    void shouldUseJacksonJsonConverterForReadableEventPayloads() {
        assertThat(config.jackson2JsonMessageConverter())
                .isInstanceOf(Jackson2JsonMessageConverter.class);
    }
}
